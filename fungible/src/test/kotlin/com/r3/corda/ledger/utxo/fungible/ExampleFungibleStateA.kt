package com.r3.corda.ledger.utxo.fungible

import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey

@BelongsToContract(ExampleFungibleContract::class)
data class ExampleFungibleStateA(
    val alice: PublicKey,
    val bob: PublicKey,
    private val quantity: NumericDecimal,
    private val identifierHash: SecureHash = SecureHash.parse("SHA256:000000000000000000000000000000000000000000000000000000000000000A")
) : FungibleState<NumericDecimal> {

    override fun getParticipants(): List<PublicKey> {
        return listOf(alice, bob)
    }

    override fun getQuantity(): NumericDecimal {
        return quantity
    }

    override fun getIdentifierHash(): SecureHash {
        return identifierHash
    }
}
