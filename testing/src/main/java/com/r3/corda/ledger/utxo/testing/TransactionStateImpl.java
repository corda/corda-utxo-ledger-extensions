package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.common.*;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

final class TransactionStateImpl<T extends ContractState> implements TransactionState<T> {

    @NotNull
    private final T contractState;

    @NotNull
    private final Party notary;

    @Nullable
    private final EncumbranceGroup encumbrance;

    public TransactionStateImpl(
            @NotNull final T contractState,
            @NotNull final Party notary,
            @Nullable final EncumbranceGroup encumbrance) {
        this.contractState = contractState;
        this.notary = notary;
        this.encumbrance = encumbrance;
    }

    @NotNull
    @Override
    public T getContractState() {
        return contractState;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getContractStateType() {
        return (Class<T>) contractState.getClass();
    }

    @NotNull
    @Override
    public Class<? extends Contract> getContractType() {
        return Contract.class; // TODO : Get the real one!
    }

    @NotNull
    @Override
    public Party getNotary() {
        return notary;
    }

    @Nullable
    @Override
    public EncumbranceGroup getEncumbranceGroup() {
        return encumbrance;
    }
}
