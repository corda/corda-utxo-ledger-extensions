package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.VisibleState;
import net.corda.v5.ledger.utxo.BelongsToContract;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateRef;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a mechanism for implementing uniquely identifiable states.
 */
@BelongsToContract(IdentifiableContract.class)
public interface IdentifiableState extends VisibleState {

    /**
     * Gets the unique ID of the current {@link IdentifiableState}, or null if it's the first state.
     *
     * @return Returns the unique ID of the current {@link IdentifiableState}, or null if it's the first state.
     */
    @Nullable
    StateRef getId();
}
