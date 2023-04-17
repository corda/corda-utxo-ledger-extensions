package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.EncumbranceGroup;
import net.corda.v5.ledger.utxo.TransactionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;

final class TransactionStateImpl<T extends ContractState> implements TransactionState<T> {

    @NotNull
    private final T contractState;

    @NotNull
    private final PublicKey notaryKey;

    @NotNull
    private final MemberX500Name notaryName;

    @Nullable
    private final EncumbranceGroup encumbrance;

    public TransactionStateImpl(
            @NotNull final T contractState,
            @NotNull final PublicKey notaryKey,
            @NotNull final MemberX500Name notaryName,
            @Nullable final EncumbranceGroup encumbrance) {
        this.contractState = contractState;
        this.notaryKey = notaryKey;
        this.notaryName = notaryName;
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
    public PublicKey getNotaryKey() {
        return notaryKey;
    }

    @NotNull
    @Override
    public MemberX500Name getNotaryName() {
        return notaryName;
    }

    @Nullable
    @Override
    public EncumbranceGroup getEncumbranceGroup() {
        return encumbrance;
    }
}
