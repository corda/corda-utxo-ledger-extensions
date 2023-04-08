package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a mechanism for implementing fungible states.
 *
 * @param <T> The underlying {@link Numeric} type represented by the fungible state.
 */
public interface FungibleState<T extends Numeric<?>> extends ContractState {

    /**
     * Gets the quantity of the current {@link FungibleState}.
     *
     * @return Returns the quantity of the current {@link FungibleState}.
     */
    @NotNull
    T getQuantity();

    /**
     * Determines whether the current {@link FungibleState} is fungible with the specified other {@link FungibleState}.
     * The default implementation only considers {@link FungibleState} instances of the same type to be fungible.
     *
     * @param other The other {@link FungibleState} to determine is fungible with the current {@link FungibleState}.
     * @return Returns true if the current {@link FungibleState} is fungible with the specified other {@link FungibleState}; otherwise, false.
     */
    boolean isFungibleWith(@NotNull FungibleState<T> other);
}
