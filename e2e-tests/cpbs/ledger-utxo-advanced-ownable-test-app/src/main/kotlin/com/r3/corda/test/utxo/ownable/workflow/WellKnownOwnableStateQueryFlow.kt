package com.r3.corda.test.utxo.ownable.workflow

import com.r3.corda.ledger.utxo.ownable.query.WellKnownOwnableStateQueries
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
import com.r3.corda.test.utxo.ownable.contract.TestOwnableContract
import com.r3.corda.test.utxo.ownable.contract.TestOwnableState
import java.time.Instant
import java.time.temporal.ChronoUnit

class WellKnownOwnableStateQueryFlow : ClientStartableFlow {

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

        val ownedState = TestOwnableState(owner = myKey, ownerName = myInfo.name, participants = listOf(myKey))
        val notOwnedState = TestOwnableState(owner = otherMemberKey, ownerName = otherMember.name, participants = listOf(myKey))

        val createTransaction = utxoLedgerService.createTransactionBuilder()
            .addOutputStates(ownedState, ownedState, notOwnedState, notOwnedState)
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(myKey)
            .addCommand(TestOwnableContract.Create())
            .toSignedTransaction()

        val stateRefs = createTransaction.outputStateAndRefs.map { it.ref }

        @Suppress("UNCHECKED_CAST")
        val query = utxoLedgerService.query(WellKnownOwnableStateQueries.GET_BY_OWNER_NAME, StateAndRef::class.java)
            .setLimit(50)
            .setParameter("ownerName", myInfo.name.toString())
            .setParameter("stateType", TestOwnableState::class.java.name) as VaultNamedParameterizedQuery<StateAndRef<TestOwnableState>>

        val statesBeforeCreation = executeQuery(query)

        utxoLedgerService.finalize(createTransaction, emptyList())

        val statesAfterCreation = executeQuery(query)

        val consumeTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(stateRefs)
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(myKey)
            .addCommand(TestOwnableContract.Delete())
            .toSignedTransaction()

        utxoLedgerService.finalize(consumeTransaction, emptyList())

        val statesAfterConsumption = executeQuery(query)

        val response = WellKnownOwnableStateQueryResponse(
            before = statesBeforeCreation.map { WellKnownOwnableStateValues(it.state.contractState.ownerName.toString()) },
            after = statesAfterCreation.map { WellKnownOwnableStateValues(it.state.contractState.ownerName.toString()) },
            consumed = statesAfterConsumption.map { WellKnownOwnableStateValues(it.state.contractState.ownerName.toString()) }
        )

        require(response.after.map { it.ownerName }.toSet().size == 1) {
            "The query returned an owner that was not the one that was queried for (${myInfo.name}) - ${response.after.map { it.ownerName }}"
        }
        require(response.after.map { it.ownerName }.toSet().single() == myInfo.name.toString()) {
            "The query returned an owner that was not the one that was queried for (${myInfo.name}) - ${response.after.map { it.ownerName }}"
        }

        return jsonMarshallingService.format(response)
    }

    @Suspendable
    private fun executeQuery(
        query: VaultNamedParameterizedQuery<StateAndRef<TestOwnableState>>
    ): List<StateAndRef<TestOwnableState>> {
        var offset = 0
        query.apply {
            setOffset(offset)
            setCreatedTimestampLimit(Instant.now())
        }
        val results = mutableListOf<StateAndRef<TestOwnableState>>()
        var resultSet = query.execute()
        while (resultSet.results.isNotEmpty()) {
            results += resultSet.results
            offset += 50
            query.setOffset(offset)
            resultSet = query.execute()
        }
        return results
    }

    data class WellKnownOwnableStateQueryResponse(
        val before: List<WellKnownOwnableStateValues>,
        val after: List<WellKnownOwnableStateValues>,
        val consumed: List<WellKnownOwnableStateValues>
    )

    data class WellKnownOwnableStateValues(val ownerName: String)
}
