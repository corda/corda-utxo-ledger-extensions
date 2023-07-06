package com.r3.corda.test.utxo.fungible.contract.testing

import com.r3.corda.ledger.utxo.fungible.FungibleState
import com.r3.corda.ledger.utxo.fungible.NumericInteger
import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey

@BelongsToContract(MyFungibleContract::class)
data class MyFungibleStateA(
    private val quantity: NumericInteger,
    private val owner: PublicKey
) : MyFungibleState {

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(owner)
    }

    override fun getQuantity(): NumericInteger {
        return quantity
    }

    override fun isFungibleWith(other: FungibleState<NumericInteger>): Boolean {
        return other is MyFungibleStateA
    }
}
