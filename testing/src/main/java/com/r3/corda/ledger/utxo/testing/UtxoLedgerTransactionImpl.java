package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.common.transaction.TransactionMetadata;
import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TimeWindow;
import net.corda.v5.ledger.utxo.TransactionState;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import net.corda.v5.membership.GroupParameters;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

final class UtxoLedgerTransactionImpl implements UtxoLedgerTransaction {

    @NotNull
    private final SecureHash id;

    @NotNull
    private final PublicKey notaryKey;

    @NotNull
    private final MemberX500Name notaryName;

    @NotNull
    private final List<Command> commands;

    @NotNull
    private final List<PublicKey> signatories;

    @NotNull
    private final List<StateAndRef<? extends ContractState>> inputStateAndRefs;

    @NotNull
    private final List<StateAndRef<? extends ContractState>> outputStateAndRefs;

    @NotNull
    private final List<StateAndRef<? extends ContractState>> referenceStateAndRefs;

    @NotNull
    private final TimeWindow timeWindow;

    public UtxoLedgerTransactionImpl(@NotNull final UtxoTransactionBuilder builder) {
        this.id = builder.getTransactionId();
        this.commands = builder.getCommands();
        this.signatories = builder.getSignatories();
        this.inputStateAndRefs = builder.getInputStateAndRefs();
        this.outputStateAndRefs = builder.getOutputStateAndRefs();
        this.referenceStateAndRefs = builder.getReferenceStateAndRefs();
        this.timeWindow = builder.getTimeWindow();
        this.notaryKey = builder.getNotaryKey();
        this.notaryName = builder.getNotaryName();
    }

    @NotNull
    @Override
    public SecureHash getId() {
        return id;
    }

    @NotNull
    @Override
    public PublicKey getNotaryKey() {
        return notaryKey;
    }

    @NotNull
    @Override
    public MemberX500Name getNotaryName() {
        return notaryName;
    }

    @NotNull
    @Override
    public TransactionMetadata getMetadata() {
        throw new UnsupportedOperationException("TODO : Implement TransactionMetadata");
    }

    @NotNull
    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @NotNull
    @Override
    public List<StateAndRef<?>> getInputStateAndRefs() {
        return inputStateAndRefs;
    }

    @NotNull
    @Override
    public List<TransactionState<?>> getInputTransactionStates() {
        return getInputStateAndRefs()
                .stream()
                .map(StateAndRef::getState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<ContractState> getInputContractStates() {
        return getInputTransactionStates()
                .stream()
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<StateRef> getInputStateRefs() {
        return getInputStateAndRefs()
                .stream()
                .map(StateAndRef::getRef)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<StateAndRef<?>> getOutputStateAndRefs() {
        return outputStateAndRefs;
    }

    @NotNull
    @Override
    public List<TransactionState<?>> getOutputTransactionStates() {
        return getOutputStateAndRefs()
                .stream()
                .map(StateAndRef::getState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<ContractState> getOutputContractStates() {
        return getOutputTransactionStates()
                .stream()
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<StateAndRef<?>> getReferenceStateAndRefs() {
        return referenceStateAndRefs;
    }

    @NotNull
    @Override
    public List<TransactionState<?>> getReferenceTransactionStates() {
        return getReferenceStateAndRefs()
                .stream()
                .map(StateAndRef::getState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<ContractState> getReferenceContractStates() {
        return getReferenceTransactionStates()
                .stream()
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<StateRef> getReferenceStateRefs() {
        return getReferenceStateAndRefs()
                .stream()
                .map(StateAndRef::getRef)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public List<PublicKey> getSignatories() {
        return signatories;
    }

    @NotNull
    @Override
    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    @NotNull
    @Override
    public <T extends Command> List<T> getCommands(@NotNull final Class<T> type) {
        return getCommands()
                .stream()
                .filter(command -> type.isAssignableFrom(command.getClass()))
                .map(type::cast)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public <T extends ContractState> List<StateAndRef<T>> getInputStateAndRefs(@NotNull final Class<T> type) {
        return getStateAndRefs(getInputStateAndRefs(), type);
    }

    @NotNull
    @Override
    public <T extends ContractState> List<T> getInputStates(@NotNull final Class<T> type) {
        return getInputStateAndRefs(type)
                .stream()
                .map(StateAndRef::getState)
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public <T extends ContractState> List<StateAndRef<T>> getOutputStateAndRefs(@NotNull final Class<T> type) {
        return getStateAndRefs(getOutputStateAndRefs(), type);
    }

    @NotNull
    @Override
    public <T extends ContractState> List<T> getOutputStates(@NotNull final Class<T> type) {
        return getOutputStateAndRefs(type)
                .stream()
                .map(StateAndRef::getState)
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    @Override
    public <T extends ContractState> List<StateAndRef<T>> getReferenceStateAndRefs(@NotNull final Class<T> type) {
        return getStateAndRefs(getReferenceStateAndRefs(), type);
    }

    @NotNull
    @Override
    public <T extends ContractState> List<T> getReferenceStates(@NotNull final Class<T> type) {
        return getReferenceStateAndRefs(type)
                .stream()
                .map(StateAndRef::getState)
                .map(TransactionState::getContractState)
                .collect(Collectors.toUnmodifiableList());
    }

    @NotNull
    private <T extends ContractState> List<StateAndRef<T>> getStateAndRefs(
            @NotNull final Iterable<StateAndRef<? extends ContractState>> stateAndRefs,
            @NotNull final Class<T> type) {
        List<StateAndRef<T>> result = new ArrayList<>();

        for (StateAndRef<? extends ContractState> stateAndRef : stateAndRefs) {
            TransactionState<? extends ContractState> transactionState = stateAndRef.getState();
            ContractState contractState = transactionState.getContractState();
            if (type.isAssignableFrom(contractState.getClass())) {
                T castState = type.cast(contractState);
                TransactionState<T> castTransactionState = new TransactionStateImpl<>(
                        castState,
                        transactionState.getNotaryKey(),
                        transactionState.getNotaryName(),
                        transactionState.getEncumbranceGroup()
                );

                result.add(new StateAndRefImpl<>(castTransactionState, stateAndRef.getRef()));
            }
        }

        return Collections.unmodifiableList(result);
    }

    @NotNull
    @Override
    public GroupParameters getGroupParameters() {
        return null;
    }
}
