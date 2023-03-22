package com.r3.corda.ledger.utxo.issuable;

import net.corda.v5.base.types.*;
import org.jetbrains.annotations.*;

public interface WellKnownIssuableState extends IssuableState {

    @NotNull
    MemberX500Name getIssuerName();
}
