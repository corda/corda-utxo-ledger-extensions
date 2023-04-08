package com.r3.corda.ledger.utxo.fungible

import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey

@BelongsToContract(ExampleFungibleContract::class)
data class ExampleFungibleStateA(
    override val alice: PublicKey,
    override val bob: PublicKey,
    private val quantity: NumericDecimal
) : ExampleFungibleState {

    override fun getParticipants(): List<PublicKey> {
        return listOf(alice, bob)
    }

    override fun getQuantity(): NumericDecimal {
        return quantity
    }

    override fun isFungibleWith(other: FungibleState<NumericDecimal>): Boolean {
        return javaClass == other.javaClass
    }
}
