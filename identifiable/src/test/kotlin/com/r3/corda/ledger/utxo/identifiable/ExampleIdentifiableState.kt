package com.r3.corda.ledger.utxo.identifiable

import net.corda.v5.ledger.utxo.StateRef
import java.security.PublicKey

data class ExampleIdentifiableState(
    val alice: PublicKey,
    val bob: PublicKey,
    private val id: StateRef? = null
) : IdentifiableState {

    override fun getParticipants(): List<PublicKey> {
        return listOf(alice, bob)
    }

    override fun getId(): StateRef? {
        return id
    }
}