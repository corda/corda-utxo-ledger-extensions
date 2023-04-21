package com.r3.corda.ledger.utxo.issuable

import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey

@BelongsToContract(ExampleIssuableContract::class)
class ExampleIssuableState(private val issuer: PublicKey) : IssuableState {

    override fun getIssuer(): PublicKey {
        return issuer
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(getIssuer())
    }
}
