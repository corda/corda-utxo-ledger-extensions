package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.common.Party;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.EncumbranceGroup;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TransactionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;

public final class TransactionBuilderState {

    @NotNull
    private final ContractState state;

    @NotNull
    private final StateRef ref;

    @NotNull
    private final Party notary;

    @Nullable
    private final String encumbrance;

    public TransactionBuilderState(
            @NotNull ContractState state,
            @NotNull StateRef ref,
            @NotNull Party notary,
            @Nullable String encumbrance) {
        this.state = state;
        this.ref = ref;
        this.notary = notary;
        this.encumbrance = encumbrance;
    }

    @NotNull
    public ContractState getState() {
        return state;
    }

    @NotNull
    public StateRef getRef() {
        return ref;
    }

    @NotNull
    public Party getNotary() {
        return notary;
    }

    @Nullable
    public String getEncumbrance() {
        return encumbrance;
    }

    @NotNull
    public TransactionState<ContractState> toTransactionState(@NotNull final Iterable<TransactionBuilderState> states) {
        if (getEncumbrance() == null) {
            return new TransactionStateImpl<>(getState(), getNotary(), null);
        }

        int size = (int) StreamSupport
                .stream(states.spliterator(), false)
                .filter(txbState -> getEncumbrance().equals(txbState.getEncumbrance()))
                .count();

        EncumbranceGroup encumbranceGroup = new EncumbranceGroupImpl(size, getEncumbrance());
        return new TransactionStateImpl<>(getState(), getNotary(), encumbranceGroup);
    }

    @NotNull
    public StateAndRef<ContractState> toStateAndRef(@NotNull final Iterable<TransactionBuilderState> states) {
        TransactionState<ContractState> transactionState = toTransactionState(states);
        return new StateAndRefImpl<>(transactionState, getRef());
    }
}
