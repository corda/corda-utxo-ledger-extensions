package com.r3.corda.ledger.utxo.ownable;

import net.corda.v5.base.types.*;
import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

public interface WellKnownOwnableState extends ContractState {

    @NotNull
    MemberX500Name getOwnerName();
}
