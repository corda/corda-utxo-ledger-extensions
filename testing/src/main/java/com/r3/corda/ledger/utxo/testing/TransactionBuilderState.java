package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.EncumbranceGroup;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TransactionState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.stream.StreamSupport;

public final class TransactionBuilderState {

    @NotNull
    private final ContractState state;

    @NotNull
    private final StateRef ref;

    @NotNull
    private final PublicKey notaryKey;

    @NotNull
    private final MemberX500Name notaryName;

    @Nullable
    private final String encumbrance;

    public TransactionBuilderState(
            @NotNull final ContractState state,
            @NotNull final StateRef ref,
            @NotNull final PublicKey notaryKey,
            @NotNull final MemberX500Name notaryName,
            @Nullable final String encumbrance) {
        this.state = state;
        this.ref = ref;
        this.notaryKey = notaryKey;
        this.notaryName = notaryName;
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
    public PublicKey getNotaryKey() {
        return notaryKey;
    }

    @NotNull
    public MemberX500Name getNotaryName() {
        return notaryName;
    }

    @Nullable
    public String getEncumbrance() {
        return encumbrance;
    }

    @NotNull
    public TransactionState<ContractState> toTransactionState(@NotNull final Iterable<TransactionBuilderState> states) {
        if (getEncumbrance() == null) {
            return new TransactionStateImpl<>(getState(), getNotaryKey(), getNotaryName(), null);
        }

        int size = (int) StreamSupport
                .stream(states.spliterator(), false)
                .filter(txbState -> getEncumbrance().equals(txbState.getEncumbrance()))
                .count();

        EncumbranceGroup encumbranceGroup = new EncumbranceGroupImpl(size, getEncumbrance());
        return new TransactionStateImpl<>(getState(), getNotaryKey(), getNotaryName(), encumbranceGroup);
    }

    @NotNull
    public StateAndRef<ContractState> toStateAndRef(@NotNull final Iterable<TransactionBuilderState> states) {
        TransactionState<ContractState> transactionState = toTransactionState(states);
        return new StateAndRefImpl<>(transactionState, getRef());
    }
}
