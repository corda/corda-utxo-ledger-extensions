package com.r3.corda.ledger.utxo.issuable;

import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

import java.security.*;

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
