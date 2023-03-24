package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.common.StaticPointer
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.StateRef
import java.security.PublicKey

@BelongsToContract(ExampleChainableContract::class)
data class ExampleChainableState(
    val alice: PublicKey,
    val bob: PublicKey,
    private val previousStatePointer: StaticPointer<ExampleChainableState>?
) : ChainableState<ExampleChainableState> {

    override fun getParticipants(): List<PublicKey> {
        return listOf(alice, bob)
    }

    override fun getPreviousStatePointer(): StaticPointer<ExampleChainableState>? {
        return previousStatePointer
    }

    override fun next(ref: StateRef): ExampleChainableState {
        return copy(previousStatePointer = StaticPointer(ref, ExampleChainableState::class.java))
    }
}
