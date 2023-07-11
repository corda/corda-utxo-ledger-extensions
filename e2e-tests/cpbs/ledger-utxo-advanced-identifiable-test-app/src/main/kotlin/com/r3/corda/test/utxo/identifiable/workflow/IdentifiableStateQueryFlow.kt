package com.r3.corda.test.utxo.identifiable.workflow

import com.r3.corda.ledger.utxo.identifiable.query.IdentifiableStateQueries
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
import com.r3.corda.test.utxo.identifiable.contract.TestIdentifiableContract
import com.r3.corda.test.utxo.identifiable.contract.TestIdentifiableState
import java.time.Instant
import java.time.temporal.ChronoUnit

class IdentifiableStateQueryFlow : ClientStartableFlow {

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
        val key = memberLookup.myInfo().firstLedgerKey
        val state = TestIdentifiableState(id = null, participants = listOf(key))
        val createTransaction = utxoLedgerService.createTransactionBuilder()
            .addOutputState(state)
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(key)
            .addCommand(TestIdentifiableContract.Create())
            .toSignedTransaction()

        val stateRefs = createTransaction.outputStateAndRefs.map { it.ref }

        @Suppress("UNCHECKED_CAST")
        val query = utxoLedgerService.query(IdentifiableStateQueries.GET_BY_IDS, StateAndRef::class.java)
            .setLimit(50)
            .setParameter("ids", stateRefs.map { it.toString() }) as VaultNamedParameterizedQuery<StateAndRef<TestIdentifiableState>>

        val statesBeforeCreation = executeQuery(query)

        utxoLedgerService.finalize(createTransaction, emptyList())

        val statesAfterCreation = executeQuery(query)

        val updateTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(stateRefs)
            .addOutputState(state.copy(id = stateRefs.single()))
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(key)
            .addCommand(TestIdentifiableContract.Update())
            .toSignedTransaction()

        utxoLedgerService.finalize(updateTransaction, emptyList())

        val statesAfterUpdate = executeQuery(query)

        val consumeTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(updateTransaction.outputStateAndRefs.map { it.ref })
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(key)
            .addCommand(TestIdentifiableContract.Delete())
            .toSignedTransaction()

        utxoLedgerService.finalize(consumeTransaction, emptyList())

        val statesAfterConsumption = executeQuery(query)

        return jsonMarshallingService.format(
            IdentifiableStateQueryResponse(
                before = statesBeforeCreation.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                after = statesAfterCreation.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                updated = statesAfterUpdate.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                consumed = statesAfterConsumption.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) }
            )
        )
    }

    @Suspendable
    private fun executeQuery(
        query: VaultNamedParameterizedQuery<StateAndRef<TestIdentifiableState>>
    ): List<StateAndRef<TestIdentifiableState>> {
        var offset = 0
        query.apply {
            setOffset(offset)
            setCreatedTimestampLimit(Instant.now())
        }
        val results = mutableListOf<StateAndRef<TestIdentifiableState>>()
        var resultSet = query.execute()
        while (resultSet.results.isNotEmpty()) {
            results += resultSet.results
            offset += 50
            resultSet = query.setOffset(offset).execute()
        }
        return results
    }

    data class IdentifiableStateQueryResponse(
        val before: List<IdentifiableStateValues>,
        val after: List<IdentifiableStateValues>,
        val updated: List<IdentifiableStateValues>,
        val consumed: List<IdentifiableStateValues>
    )

    data class IdentifiableStateValues(val stateRef: String, val id: String?)
}
