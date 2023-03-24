package com.r3.corda.ledger.utxo.issuable;

import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

/**
 * Defines a mechanism for implementing issuable states.
 */
public interface IssuableState extends ContractState {

    /**
     * Gets the issuer's {@link  PublicKey} of the current {@link IssuableState}.
     *
     * @return Returns the issuer's {@link  PublicKey} of the current {@link IssuableState}.
     */
    @NotNull
    PublicKey getIssuer();
}
