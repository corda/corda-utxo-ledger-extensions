package com.r3.corda.demo.utxo.chainable.contract

import net.corda.v5.ledger.utxo.StateAndRef
import java.security.PublicKey

fun StateAndRef<Vehicle>.transferOwnership(owner: PublicKey): Vehicle {
    return state.contractState.next(ref).copy(owner = owner)
}
