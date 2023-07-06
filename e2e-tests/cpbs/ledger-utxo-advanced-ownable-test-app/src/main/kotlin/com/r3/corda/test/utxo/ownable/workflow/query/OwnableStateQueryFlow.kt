package com.r3.corda.test.utxo.ownable.workflow.query

import com.r3.corda.ledger.utxo.ownable.query.OwnableStateQueries
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
import com.r3.corda.test.utxo.ownable.contract.TestOwnableContract
import com.r3.corda.test.utxo.ownable.contract.TestOwnableState
import com.r3.corda.test.utxo.ownable.workflow.firstLedgerKey
import java.security.PublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit

class OwnableStateQueryFlow : ClientStartableFlow {

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

        val ownedState = TestOwnableState(owner = myKey, ownerName = memberLookup.myInfo().name, participants = listOf(myKey))
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
        val query = utxoLedgerService.query(OwnableStateQueries.GET_BY_OWNER, StateAndRef::class.java)
            .setLimit(50)
            .setParameter("owner", getKeyId(myKey))
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

        val response = OwnableStateQueryResponse(
            before = statesBeforeCreation.map { OwnableStateValues(getKeyId(it)) },
            after = statesAfterCreation.map { OwnableStateValues(getKeyId(it)) },
            consumed = statesAfterConsumption.map { OwnableStateValues(getKeyId(it)) }
        )

        require(response.after.map { it.owner }.toSet().size == 1) {
            "The query returned an owner that was not the one that was queried for (${getKeyId(myKey)}) - ${response.after.map { it.owner }}"
        }
        require(response.after.map { it.owner }.toSet().single() == getKeyId(myKey)) {
            "The query returned an owner that was not the one that was queried for (${getKeyId(myKey)}) - ${response.after.map { it.owner }}"
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

    private fun getKeyId(publicKey: PublicKey): String {
        return digestService.hash(publicKey.encoded, DigestAlgorithmName.SHA2_256).toString()
    }

    private fun getKeyId(stateAndRef: StateAndRef<TestOwnableState>): String {
        return getKeyId(stateAndRef.state.contractState.owner)
    }

    data class OwnableStateQueryResponse(
        val before: List<OwnableStateValues>,
        val after: List<OwnableStateValues>,
        val consumed: List<OwnableStateValues>
    )

    data class OwnableStateValues(val owner: String)
}
