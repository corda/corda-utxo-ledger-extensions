package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.crypto.*;
import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mechanism for implementing fungible states.
 *
 * @param <T> The underlying {@link Numeric} type represented by the fungible state.
 */
@BelongsToContract(FungibleContract.class)
public interface FungibleState<T extends Numeric<?>> extends ContractState {

    /**
     * Gets the quantity of the current {@link FungibleState}.
     *
     * @return Returns the quantity of the current {@link FungibleState}.
     */
    @NotNull
    T getQuantity();

    /**
     * Gets the unique identifier {@link SecureHash} of the current {@link FungibleState}.
     *
     * @return Returns the unique identifier {@link SecureHash} of the current {@link FungibleState}.
     */
    @NotNull
    SecureHash getIdentifierHash();
}
