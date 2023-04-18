package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.utxo.Attachment;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TimeWindow;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class UtxoTransactionBuilder {

    @NotNull
    private final SecureHash transactionId;

    @NotNull
    private final PublicKey notaryKey;

    @NotNull
    private final MemberX500Name notaryName;

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

    public UtxoTransactionBuilder(@NotNull final PublicKey notaryKey, @NotNull final MemberX500Name notaryName) {
        this.notaryKey = notaryKey;
        this.notaryName = notaryName;
        this.transactionId = ContractTestUtils.createRandomSecureHash();
        this.timeWindow = new TimeWindowBetweenImpl(Instant.MIN, Instant.MAX);
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
            @NotNull final PublicKey notaryKey,
            @NotNull final MemberX500Name notaryName,
            @Nullable final String encumbrance) {
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notaryKey, notaryName, encumbrance);
        return addInputState(transactionBuilderState);
    }

    @NotNull
    public UtxoTransactionBuilder addInputState(@NotNull final ContractState state) {
        return addInputState(state, ContractTestUtils.createRandomStateRef(), notaryKey, notaryName, null);
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
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notaryKey, notaryName, encumbrance);
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
            @NotNull final PublicKey notaryKey,
            @NotNull final MemberX500Name notaryName,
            @Nullable final String encumbrance) {
        TransactionBuilderState transactionBuilderState = new TransactionBuilderState(state, ref, notaryKey, notaryName, encumbrance);
        return addReferenceState(transactionBuilderState);
    }

    @NotNull
    public UtxoTransactionBuilder addReferenceState(@NotNull final ContractState state) {
        return addReferenceState(state, ContractTestUtils.createRandomStateRef(), notaryKey, notaryName, null);
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
    public PublicKey getNotaryKey() {
        return notaryKey;
    }

    @NotNull
    public MemberX500Name getNotaryName() {
        return notaryName;
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
