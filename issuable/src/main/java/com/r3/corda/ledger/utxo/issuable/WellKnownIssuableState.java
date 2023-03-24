package com.r3.corda.ledger.utxo.issuable;

import net.corda.v5.base.types.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mechanism for implementing well-known issuable states.
 */
public interface WellKnownIssuableState extends IssuableState {

    /**
     * Gets the issuer's {@link  MemberX500Name} of the current {@link WellKnownIssuableState}.
     *
     * @return Returns the issuer's {@link  MemberX500Name} of the current {@link WellKnownIssuableState}.
     */
    @NotNull
    MemberX500Name getIssuerName();
}
