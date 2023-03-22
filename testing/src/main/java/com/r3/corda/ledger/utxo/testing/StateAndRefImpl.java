package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TransactionState;
import org.jetbrains.annotations.NotNull;

final class StateAndRefImpl<T extends ContractState> implements StateAndRef<T> {

    @NotNull
    private final StateRef ref;

    @NotNull
    private final TransactionState<T> state;

    public StateAndRefImpl(@NotNull final TransactionState<T> state, @NotNull final StateRef ref) {
        this.state = state;
        this.ref = ref;
    }

    @NotNull
    @Override
    public TransactionState<T> getState() {
        return state;
    }

    @NotNull
    @Override
    public StateRef getRef() {
        return ref;
    }
}
