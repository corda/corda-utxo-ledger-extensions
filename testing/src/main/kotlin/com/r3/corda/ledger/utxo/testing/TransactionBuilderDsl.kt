package com.r3.corda.ledger.utxo.testing

import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.common.Party
import net.corda.v5.ledger.utxo.Attachment
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.ContractState
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.TimeWindow
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import java.security.PublicKey
import java.time.Instant

class TransactionBuilderDsl(private val notary: Party) {

    private val builder = UtxoTransactionBuilder(notary)

    @TransactionBuilderDslMarker
    fun addAttachment(attachment: Attachment): UtxoTransactionBuilder {
        return builder.addAttachment(attachment)
    }

    @TransactionBuilderDslMarker
    fun addCommand(command: Command): UtxoTransactionBuilder {
        return builder.addCommand(command)
    }

    @TransactionBuilderDslMarker
    fun addSignatory(signatory: PublicKey): UtxoTransactionBuilder {
        return builder.addSignatory(signatory)
    }

    @TransactionBuilderDslMarker
    fun addSignatories(signatories: Iterable<PublicKey>): UtxoTransactionBuilder {
        return builder.addSignatories(signatories)
    }

    @TransactionBuilderDslMarker
    fun addSignatories(vararg signatories: PublicKey): UtxoTransactionBuilder {
        return builder.addSignatories(*signatories)
    }

    @TransactionBuilderDslMarker
    fun addInputState(state: TransactionBuilderState): UtxoTransactionBuilder {
        return builder.addInputState(state)
    }

    @TransactionBuilderDslMarker
    fun addInputState(state: ContractState): UtxoTransactionBuilder {
        return builder.addInputState(state)
    }

    @TransactionBuilderDslMarker
    fun addInputState(
        state: ContractState,
        ref: StateRef,
        notary: Party,
        encumbrance: String?
    ): UtxoTransactionBuilder {
        return builder.addInputState(state, ref, notary, encumbrance)
    }

    @TransactionBuilderDslMarker
    fun addOutputState(state: TransactionBuilderState): UtxoTransactionBuilder {
        return builder.addOutputState(state)
    }

    @TransactionBuilderDslMarker
    fun addOutputState(state: ContractState): UtxoTransactionBuilder {
        return builder.addOutputState(state)
    }

    @TransactionBuilderDslMarker
    fun addOutputState(state: ContractState, encumbrance: String?): UtxoTransactionBuilder {
        return builder.addOutputState(state, encumbrance)
    }

    @TransactionBuilderDslMarker
    fun addReferenceState(state: TransactionBuilderState): UtxoTransactionBuilder {
        return builder.addReferenceState(state)
    }

    @TransactionBuilderDslMarker
    fun addReferenceState(state: ContractState): UtxoTransactionBuilder {
        return builder.addReferenceState(state)
    }

    @TransactionBuilderDslMarker
    fun addReferenceState(
        state: ContractState,
        ref: StateRef,
        notary: Party,
        encumbrance: String?
    ): UtxoTransactionBuilder {
        return builder.addReferenceState(state, ref, notary, encumbrance)
    }

    @TransactionBuilderDslMarker
    fun setTimeWindowBetween(from: Instant, until: Instant): UtxoTransactionBuilder {
        return builder.setTimeWindowBetween(from, until)
    }

    @TransactionBuilderDslMarker
    fun setTimeWindowUntil(until: Instant): UtxoTransactionBuilder {
        return builder.setTimeWindowUntil(until)
    }

    @TransactionBuilderDslMarker
    fun getTransactionId(): SecureHash {
        return builder.transactionId
    }

    @TransactionBuilderDslMarker
    fun getNotary(): Party {
        return builder.notary
    }

    @TransactionBuilderDslMarker
    fun getAttachments(): List<Attachment> {
        return builder.attachments
    }

    @TransactionBuilderDslMarker
    fun getCommands(): List<Command> {
        return builder.commands
    }

    @TransactionBuilderDslMarker
    fun getSignatories(): List<PublicKey> {
        return builder.signatories
    }

    @TransactionBuilderDslMarker
    fun getInputStateAndRefs(): List<StateAndRef<*>> {
        return builder.inputStateAndRefs
    }

    @TransactionBuilderDslMarker
    fun getOutputStateAndRefs(): List<StateAndRef<*>> {
        return builder.outputStateAndRefs
    }

    @TransactionBuilderDslMarker
    fun getReferenceStateAndRefs(): List<StateAndRef<*>> {
        return builder.referenceStateAndRefs
    }

    @TransactionBuilderDslMarker
    fun getTimeWindow(): TimeWindow {
        return builder.timeWindow
    }

    @TransactionBuilderDslMarker
    fun toLedgerTransaction(): UtxoLedgerTransaction {
        return builder.toLedgerTransaction()
    }
}
