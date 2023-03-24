package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mechanism for implementing strictly linear state chains.
 *
 * @param <T> The underlying type of the {@link ChainableState}.
 */
@BelongsToContract(ChainableContract.class)
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

    /**
     * Creates the next {@link ChainableState} in the chain.
     *
     * @param ref The {@link StateRef} of the previous {@link ChainableState} in the chain.
     * @return Returns the next {@link ChainableState} in the chain.
     */
    @NotNull
    T next(@NotNull StateRef ref);
}
