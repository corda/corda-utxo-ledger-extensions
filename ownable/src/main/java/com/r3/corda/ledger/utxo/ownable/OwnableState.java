package com.r3.corda.ledger.utxo.ownable;

import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

/**
 * Defines a mechanism for implementing ownable states.
 */
public interface OwnableState extends ContractState {

    /**
     * Gets the owner's {@link  PublicKey} of the current {@link OwnableState}.
     *
     * @return Returns the owner's {@link  PublicKey} of the current {@link OwnableState}.
     */
    @NotNull
    PublicKey getOwner();
}
