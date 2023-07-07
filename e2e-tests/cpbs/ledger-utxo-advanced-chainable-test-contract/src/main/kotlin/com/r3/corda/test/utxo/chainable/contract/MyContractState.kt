package com.r3.corda.test.utxo.chainable.contract

import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.UUID

@BelongsToContract(MyChainableContract::class)
data class MyContractState(
    val id: UUID,
) : ContractState {

    override fun getParticipants(): List<PublicKey> {
        return emptyList()
    }
}
