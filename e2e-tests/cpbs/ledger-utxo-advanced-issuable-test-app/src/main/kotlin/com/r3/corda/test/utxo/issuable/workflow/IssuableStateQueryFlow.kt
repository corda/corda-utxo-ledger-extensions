package com.r3.corda.test.utxo.issuable.workflow

import com.r3.corda.ledger.utxo.issuable.query.IssuableStateQueries
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.crypto.DigestAlgorithmName
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.query.VaultNamedParameterizedQuery
import com.r3.corda.test.utxo.issuable.contract.TestIssuableContract
import com.r3.corda.test.utxo.issuable.contract.TestIssuableState
import java.security.PublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit

class IssuableStateQueryFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @CordaInject
    private lateinit var digestService: DigestService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val notary = notaryLookup.notaryServices.first().name

        val myInfo = memberLookup.myInfo()
        val myKey = myInfo.firstLedgerKey
        val otherMember = (memberLookup.lookup() - myInfo).first()
        val otherMemberKey = otherMember.firstLedgerKey

        val ownedState = TestIssuableState(issuer = myKey, issuerName = memberLookup.myInfo().name, participants = listOf(myKey))
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
        val query = utxoLedgerService.query(IssuableStateQueries.GET_BY_ISSUER, StateAndRef::class.java)
            .setLimit(50)
            .setParameter("issuer", getKeyId(myKey))
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

        val response = IssuableStateQueryResponse(
            before = statesBeforeCreation.map { IssuableStateValues(getKeyId(it)) },
            after = statesAfterCreation.map { IssuableStateValues(getKeyId(it)) },
            consumed = statesAfterConsumption.map { IssuableStateValues(getKeyId(it)) }
        )

        require(response.after.map { it.issuer }.toSet().size == 1) {
            "The query returned an issuer that was not the one that was queried for (${getKeyId(myKey)}) - ${response.after.map { it.issuer }}"
        }
        require(response.after.map { it.issuer }.toSet().single() == getKeyId(myKey)) {
            "The query returned an issuer that was not the one that was queried for (${getKeyId(myKey)}) - ${response.after.map { it.issuer }}"
        }

        return jsonMarshallingService.format(response)
    }

    @Suspendable
    private fun executeQuery(
        query: VaultNamedParameterizedQuery<StateAndRef<TestIssuableState>>
    ): List<StateAndRef<TestIssuableState>> {
        var offset = 0
        query.apply {
            setOffset(offset)
            setCreatedTimestampLimit(Instant.now())
        }
        val results = mutableListOf<StateAndRef<TestIssuableState>>()
        var resultSet = query.execute()
        while (resultSet.results.isNotEmpty()) {
            results += resultSet.results
            offset += 50
            query.setOffset(offset)
            resultSet = query.execute()
        }
        return results
    }

    private fun getKeyId(publicKey: PublicKey): String {
        return digestService.hash(publicKey.encoded, DigestAlgorithmName.SHA2_256).toString()
    }

    private fun getKeyId(stateAndRef: StateAndRef<TestIssuableState>): String {
        return getKeyId(stateAndRef.state.contractState.issuer)
    }

    data class IssuableStateQueryResponse(
        val before: List<IssuableStateValues>,
        val after: List<IssuableStateValues>,
        val consumed: List<IssuableStateValues>
    )

    data class IssuableStateValues(val issuer: String)
}
