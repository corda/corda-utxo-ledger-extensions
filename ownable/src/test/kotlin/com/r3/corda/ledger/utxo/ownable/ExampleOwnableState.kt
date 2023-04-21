package com.r3.corda.ledger.utxo.ownable

import java.security.PublicKey

class ExampleOwnableState(private val owner: PublicKey) : OwnableState {

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(getOwner())
    }
}
