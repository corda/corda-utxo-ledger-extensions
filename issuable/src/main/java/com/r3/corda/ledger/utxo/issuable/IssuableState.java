package com.r3.corda.ledger.utxo.issuable;

import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

import java.security.*;

public interface IssuableState extends ContractState {

    @NotNull
    PublicKey getIssuer();
}
