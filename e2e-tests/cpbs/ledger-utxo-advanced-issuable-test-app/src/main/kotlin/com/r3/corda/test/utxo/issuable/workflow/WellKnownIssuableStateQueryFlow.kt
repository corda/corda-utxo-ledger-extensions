package com.r3.corda.test.utxo.issuable.workflow

import com.r3.corda.ledger.utxo.issuable.query.WellKnownIssuableStateQueries
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.query.VaultNamedParameterizedQuery
import com.r3.corda.test.utxo.issuable.contract.TestIssuableContract
import com.r3.corda.test.utxo.issuable.contract.TestIssuableState
import java.time.Instant
import java.time.temporal.ChronoUnit

class WellKnownIssuableStateQueryFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val notary = notaryLookup.notaryServices.first().name

        val myInfo = memberLookup.myInfo()
        val myKey = myInfo.firstLedgerKey
        val otherMember = (memberLookup.lookup() - myInfo).first()
        val otherMemberKey = otherMember.firstLedgerKey

        val ownedState = TestIssuableState(issuer = myKey, issuerName = myInfo.name, participants = listOf(myKey))
        val notOwnedState = TestIssuableState(issuer = otherMemberKey, issuerName = otherMember.name, participants = listOf(myKey))

        val createTransaction = utxoLedgerService.createTransactionBuilder()
            .addOutputStates(ownedState, ownedState, notOwnedState, notOwnedState)
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(myKey)
            .addCommand(TestIssuableContract.Create())
            .toSignedTransaction()

        val stateRefs = createTransaction.outputStateAndRefs.map { it.ref }

        @Suppress("UNCHECKED_CAST")
        val query = utxoLedgerService.query(WellKnownIssuableStateQueries.GET_BY_ISSUER_NAME, StateAndRef::class.java)
            .setLimit(50)
            .setParameter("issuerName", myInfo.name.toString())
            .setParameter("stateType", TestIssuableState::class.java.name) as VaultNamedParameterizedQuery<StateAndRef<TestIssuableState>>

        val statesBeforeCreation = executeQuery(query)

        utxoLedgerService.finalize(createTransaction, emptyList())

        val statesAfterCreation = executeQuery(query)

        val consumeTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(stateRefs)
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(myKey)
            .addCommand(TestIssuableContract.Delete())
            .toSignedTransaction()

        utxoLedgerService.finalize(consumeTransaction, emptyList())

        val statesAfterConsumption = executeQuery(query)

        val response = WellKnownIssuableStateQueryResponse(
            before = statesBeforeCreation.map { WellKnownIssuableStateValues(it.state.contractState.issuerName.toString()) },
            after = statesAfterCreation.map { WellKnownIssuableStateValues(it.state.contractState.issuerName.toString()) },
            consumed = statesAfterConsumption.map { WellKnownIssuableStateValues(it.state.contractState.issuerName.toString()) }
        )

        require(response.after.map { it.issuerName }.toSet().size == 1) {
            "The query returned an issuer that was not the one that was queried for (${myInfo.name}) - ${response.after.map { it.issuerName }}"
        }
        require(response.after.map { it.issuerName }.toSet().single() == myInfo.name.toString()) {
            "The query returned an issuer that was not the one that was queried for (${myInfo.name}) - ${response.after.map { it.issuerName }}"
        }

        return jsonMarshallingService.format(response)
    }

    @Suspendable
    private fun executeQuery(
        query: VaultNamedParameterizedQuery<StateAndRef<TestIssuableState>>
    ): List<StateAndRef<TestIssuableState>> {
        query.apply {
            setCreatedTimestampLimit(Instant.now())
        }
        val results = mutableListOf<StateAndRef<TestIssuableState>>()
        val resultSet = query.execute()
        results += resultSet.results
        while (resultSet.hasNext()) {
            results += resultSet.next()
        }
        return results
    }

    data class WellKnownIssuableStateQueryResponse(
        val before: List<WellKnownIssuableStateValues>,
        val after: List<WellKnownIssuableStateValues>,
        val consumed: List<WellKnownIssuableStateValues>
    )

    data class WellKnownIssuableStateValues(val issuerName: String)
}
