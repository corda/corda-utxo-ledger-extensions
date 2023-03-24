package com.r3.corda.ledger.utxo.common;

import net.corda.v5.base.annotations.*;
import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Defines a mechanism for implementing pointers to other states on the ledger.
 *
 * @param <T> The underlying type of the {@link ContractState} instance(s) being pointed to.
 */
@CordaSerializable
public interface StatePointer<T extends ContractState> {

    /**
     * Determines whether the current {@link StatePointer} is pointing to the specified {@link StateAndRef} instance.
     *
     * @param stateAndRef The {@link StateAndRef} to check.
     * @return Returns true if the current {@link StatePointer} is pointing to the specified {@link StateAndRef}; otherwise, false.
     */
    boolean isPointingTo(@NotNull StateAndRef<T> stateAndRef);

    /**
     * Resolves the current {@link StatePointer} to a {@link List} of {@link StateAndRef} of type {@link T}.
     *
     * @param service The {@link UtxoLedgerService} that will be used to resolve {@link StateAndRef} instances of type {@link T} from the vault.
     * @return Returns a {@link List} of {@link StateAndRef} of type {@link T} resolved by this pointer.
     */
    @NotNull
    List<StateAndRef<T>> resolve(@NotNull UtxoLedgerService service);

    /**
     * Resolves the current {@link StatePointer} to a {@link List} of {@link StateAndRef} of type {@link T}.
     *
     * @param transaction The {@link UtxoLedgerTransaction} from which to resolve {@link ContractState} instances.
     * @param position    The position in the transaction from which to resolve {@link ContractState} instances.
     * @return Returns a {@link List} of {@link StateAndRef} of type {@link T} resolved by this pointer.
     */
    @NotNull
    List<StateAndRef<T>> resolve(@NotNull UtxoLedgerTransaction transaction, @NotNull StatePosition position);
}

