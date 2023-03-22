package com.r3.corda.ledger.utxo.ownable;

import net.corda.v5.ledger.utxo.*;
import org.jetbrains.annotations.*;

import java.security.*;

public interface OwnableState extends ContractState {

    @NotNull
    PublicKey getOwner();
}
