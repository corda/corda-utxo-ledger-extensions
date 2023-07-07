package com.r3.corda.test.utxo.identifiable.workflow

import com.r3.corda.ledger.utxo.identifiable.IdentifiablePointer
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService
import com.r3.corda.test.utxo.identifiable.contract.TestIdentifiableContract
import com.r3.corda.test.utxo.identifiable.contract.TestIdentifiableState
import java.time.Instant
import java.time.temporal.ChronoUnit

class IdentifiablePointerFlow : ClientStartableFlow {

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

        val pointer = IdentifiablePointer(stateRefs.single(), TestIdentifiableState::class.java)

        val statesBeforeCreation = pointer.resolve(utxoLedgerService)

        utxoLedgerService.finalize(createTransaction, emptyList())

        val statesAfterCreation = pointer.resolve(utxoLedgerService)

        val updateTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(stateRefs)
            .addOutputState(state.copy(id = stateRefs.single()))
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(key)
            .addCommand(TestIdentifiableContract.Update())
            .toSignedTransaction()

        utxoLedgerService.finalize(updateTransaction, emptyList())

        val statesAfterUpdate = pointer.resolve(utxoLedgerService)

        val consumeTransaction = utxoLedgerService.createTransactionBuilder()
            .addInputStates(updateTransaction.outputStateAndRefs.map { it.ref })
            .setTimeWindowUntil(Instant.now().plus(1, ChronoUnit.HOURS))
            .setNotary(notary)
            .addSignatories(key)
            .addCommand(TestIdentifiableContract.Delete())
            .toSignedTransaction()

        utxoLedgerService.finalize(consumeTransaction, emptyList())

        val statesAfterConsumption = pointer.resolve(utxoLedgerService)

        return jsonMarshallingService.format(
            IdentifiableStateQueryResponse(
                before = statesBeforeCreation.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                after = statesAfterCreation.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                updated = statesAfterUpdate.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) },
                consumed = statesAfterConsumption.map { IdentifiableStateValues(it.ref.toString(), it.state.contractState.id?.toString()) }
            )
        )
    }

    data class IdentifiableStateQueryResponse(
        val before: List<IdentifiableStateValues>,
        val after: List<IdentifiableStateValues>,
        val updated: List<IdentifiableStateValues>,
        val consumed: List<IdentifiableStateValues>
    )

    data class IdentifiableStateValues(val stateRef: String, val id: String?)
}
