package com.r3.corda.ledger.utxo.identifiable;

import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateRef;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a mechanism for implementing uniquely identifiable states.
 */
public interface IdentifiableState extends ContractState {

    /**
     * Gets the unique ID of the current {@link IdentifiableState}, or null if it's the first state.
     *
     * @return Returns the unique ID of the current {@link IdentifiableState}, or null if it's the first state.
     */
    @Nullable
    StateRef getId();
}
