package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.MemberX500Name;

import java.security.PublicKey;

public class Party {

    private final MemberX500Name name;
    private final PublicKey key;

    public Party(MemberX500Name name, PublicKey key) {
        this.name = name;
        this.key = key;
    }

    public MemberX500Name getName() {
        return name;
    }

    public PublicKey getKey() {
        return key;
    }
}
