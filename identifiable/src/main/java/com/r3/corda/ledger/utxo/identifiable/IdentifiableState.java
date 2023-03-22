package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mechanism for implementing uniquely identifiable states.
 */
@BelongsToContract(IdentifiableContract.class)
public interface IdentifiableState extends VisibleState {

    /**
     * Gets the unique ID of the current {@link IdentifiableState}.
     *
     * @return Returns the unique ID of the current {@link IdentifiableState}.
     */
    @Nullable
    StateRef getId();
}
