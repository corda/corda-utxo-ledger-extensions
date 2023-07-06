package com.r3.corda.demo.utxo.chainable.contract.testing

import com.r3.corda.ledger.utxo.base.StaticPointer
import com.r3.corda.ledger.utxo.chainable.ChainableState
import com.r3.corda.ledger.utxo.ownable.OwnableState
import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey
import java.util.UUID

@BelongsToContract(MyChainableContract::class)
data class MyChainableState(
    val id: UUID,
    private val owner: PublicKey,
    private val previousStatePointer: StaticPointer<MyChainableState>?
) : ChainableState<MyChainableState>, OwnableState {

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(owner)
    }

    override fun getPreviousStatePointer(): StaticPointer<MyChainableState>? {
        return previousStatePointer
    }
}
