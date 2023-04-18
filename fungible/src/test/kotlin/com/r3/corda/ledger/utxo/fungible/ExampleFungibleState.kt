package com.r3.corda.ledger.utxo.fungible

import java.security.PublicKey

sealed interface ExampleFungibleState : FungibleState<NumericDecimal> {
    val alice: PublicKey
    val bob: PublicKey
}
