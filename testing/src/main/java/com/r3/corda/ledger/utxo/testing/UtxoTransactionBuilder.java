package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.crypto.*;
import net.corda.v5.ledger.common.*;
import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.security.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

public final class UtxoTransactionBuilder {

    @NotNull
    private final SecureHash transactionId;

    @NotNull
    private final Party notary;

    @NotNull
    private final List<Attachment> attachments = new ArrayList<>();

    @NotNull
    private final List<Command> commands = new ArrayList<>();

    @NotNull
    private final List<PublicKey> signatories = new ArrayList<>();

    @NotNull
    private final List<TransactionBuilderState> inputs = new ArrayList<>();

    @NotNull
    private final List<TransactionBuilderState> outputs = new ArrayList<>();

    @NotNull
    private final List<TransactionBuilderState> references = new ArrayList<>();

    @NotNull
    private TimeWindow timeWindow;

    private int outputIndex = 0;

    public UtxoTransactionBuilder(@NotNull final Party notary) {
        this.notary = notary;
        this.transactionId = ContractTestUtils.createRandomSecureHash();
        setTimeWindowBetween(Instant.MIN, Instant.MAX);
    }

    @NotNull
    public UtxoTransactionBuilder addAttachment(@NotNull final Attachment attachment) {
        attachments.add(attachment);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addCommand(@NotNull final Command command) {
        commands.add(command);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addSignatory(@NotNull final PublicKey signatory) {
        signatories.add(signatory);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addSignatories(@NotNull final Iterable<PublicKey> signatories) {
        this.signatories.addAll(StreamSupport.stream(signatories.spliterator(), false).collect(Collectors.toList()));
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addSignatories(@NotNull final PublicKey... signatories) {
        return addSignatories(Arrays.asList(signatories));
    }

    @NotNull
    public UtxoTransactionBuilder addInputState(@NotNull final TransactionBuilderState state) {
        inputs.add(state);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addInputState(
            @NotNull final ContractState state,
            @NotNull final StateRef ref,
            @NotNull final Party notary,
            @Nullable final String encumbrance) {
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notary, encumbrance);
        return addInputState(transactionBuilderState);
    }

    @NotNull
    public UtxoTransactionBuilder addInputState(@NotNull final ContractState state) {
        return addInputState(state, ContractTestUtils.createRandomStateRef(), notary, null);
    }

    @NotNull
    public UtxoTransactionBuilder addOutputState(@NotNull final TransactionBuilderState state) {
        outputs.add(state);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addOutputState(
            @NotNull final ContractState state,
            @Nullable final String encumbrance) {
        StateRef ref = getNextOutputStateRef();
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notary, encumbrance);
        return addOutputState(transactionBuilderState);
    }

    @NotNull
    public UtxoTransactionBuilder addOutputState(@NotNull final ContractState state) {
        return addOutputState(state, null);
    }

    @NotNull
    public UtxoTransactionBuilder addReferenceState(@NotNull final TransactionBuilderState state) {
        references.add(state);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder addReferenceState(
            @NotNull final ContractState state,
            @NotNull final StateRef ref,
            @NotNull final Party notary,
            @Nullable final String encumbrance) {
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notary, encumbrance);
        return addReferenceState(transactionBuilderState);
    }

    @NotNull
    public UtxoTransactionBuilder addReferenceState(@NotNull final ContractState state) {
        return addReferenceState(state, ContractTestUtils.createRandomStateRef(), notary, null);
    }

    @NotNull
    public UtxoTransactionBuilder setTimeWindowBetween(@NotNull final Instant from, @NotNull final Instant until) {
        timeWindow = new TimeWindowBetweenImpl(from, until);
        return this;
    }

    @NotNull
    public UtxoTransactionBuilder setTimeWindowUntil(@NotNull final Instant until) {
        timeWindow = new TimeWindowUntilImpl(until);
        return this;
    }

    @NotNull
    public SecureHash getTransactionId() {
        return transactionId;
    }

    @NotNull
    public Party getNotary() {
        return notary;
    }

    @NotNull
    public List<Attachment> getAttachments() {
        return attachments;
    }

    @NotNull
    public List<Command> getCommands() {
        return commands;
    }

    @NotNull
    public List<PublicKey> getSignatories() {
        return signatories;
    }

    @NotNull
    public List<StateAndRef<? extends ContractState>> getInputStateAndRefs() {
        List<StateAndRef<ContractState>> result = new ArrayList<>();

        for (TransactionBuilderState input : inputs) {
            result.add(input.toStateAndRef(inputs));
        }

        return Collections.unmodifiableList(result);
    }

    @NotNull
    public List<StateAndRef<? extends ContractState>> getOutputStateAndRefs() {
        List<StateAndRef<ContractState>> result = new ArrayList<>();

        for (TransactionBuilderState input : outputs) {
            result.add(input.toStateAndRef(outputs));
        }

        return Collections.unmodifiableList(result);
    }

    @NotNull
    public List<StateAndRef<? extends ContractState>> getReferenceStateAndRefs() {
        List<StateAndRef<ContractState>> result = new ArrayList<>();

        for (TransactionBuilderState input : references) {
            result.add(input.toStateAndRef(references));
        }

        return Collections.unmodifiableList(result);
    }

    @NotNull
    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    @NotNull
    public UtxoLedgerTransaction toLedgerTransaction() {
        return new UtxoLedgerTransactionImpl(this);
    }

    private StateRef getNextOutputStateRef() {
        return new StateRef(transactionId, outputIndex++);
    }
}
