package com.r3.corda.test.utxo.identifiable.contract

import com.r3.corda.ledger.utxo.identifiable.IdentifiableState
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.StateRef
import java.security.PublicKey

@BelongsToContract(MyIdentifiableContract::class)
data class MyIdentifiableState(
    private val id: StateRef?,
    val owner: PublicKey,
) : IdentifiableState {

    override fun getId(): StateRef? {
        return id
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(owner)
    }
}
