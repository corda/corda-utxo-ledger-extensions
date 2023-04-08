package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.StaticPointer;
import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a mechanism for implementing strictly linear state chains.
 *
 * @param <T> The underlying type of the {@link ChainableState}.
 */
public interface ChainableState<T extends ChainableState<T>> extends ContractState {

    /**
     * Gets a {@link StaticPointer} to the previous {@link ChainableState} instance in the chain,
     * or null if the current instance is the first instance in the chain.
     *
     * @return Returns a {@link StaticPointer} to the previous {@link ChainableState} instance in the chain,
     * or null if the current instance is the first instance in the chain.
     */
    @Nullable
    StaticPointer<T> getPreviousStatePointer();
}
