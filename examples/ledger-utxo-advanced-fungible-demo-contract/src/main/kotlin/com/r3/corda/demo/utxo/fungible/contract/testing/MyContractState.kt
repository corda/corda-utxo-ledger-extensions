package com.r3.corda.demo.utxo.fungible.contract.testing

import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.ContractState
import java.security.PublicKey
import java.util.UUID

@BelongsToContract(MyFungibleContract::class)
data class MyContractState(
    val id: UUID,
) : ContractState {

    override fun getParticipants(): List<PublicKey> {
        return emptyList()
    }
}
