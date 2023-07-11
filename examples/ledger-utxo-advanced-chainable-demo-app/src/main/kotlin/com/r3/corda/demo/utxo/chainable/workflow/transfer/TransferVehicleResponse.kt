package com.r3.corda.demo.utxo.chainable.workflow.transfer

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.chainable.contract.Vehicle
import com.r3.corda.demo.utxo.chainable.workflow.getMemberX500Name
import java.util.UUID

data class TransferVehicleResponse(
    val make: String,
    val model: String,
    val id: UUID,
    val manufacturer: String,
    val owner: String
) {
    internal companion object {

        @Suppress("UNCHECKED_CAST")
        fun fromTransaction(transaction: UtxoSignedTransaction, memberLookup: MemberLookup): TransferVehicleResponse {
            val vehicle = transaction
                .outputStateAndRefs
                .map { it.state.contractState }
                .filterIsInstance<Vehicle>()
                .single()

            val manufacturer = memberLookup.getMemberX500Name(vehicle.manufacturer).toString()
            val owner = memberLookup.getMemberX500Name(vehicle.owner).toString()

            return with(vehicle) { TransferVehicleResponse(make, model, id, manufacturer, owner) }
        }
    }
}
