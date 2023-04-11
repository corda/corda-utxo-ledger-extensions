package net.cordapp.demo.utxo.chainable.contract.transfer

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.chainable.contract.Vehicle
import net.cordapp.demo.utxo.chainable.contract.issue.IssueVehicleResponse
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
        fun fromTransaction(transaction: UtxoSignedTransaction): IssueVehicleResponse {
            val vehicle = transaction
                .outputStateAndRefs
                .map { it.state.contractState }
                .filterIsInstance<Vehicle>()
                .single()

            return with(vehicle) { IssueVehicleResponse(make, model, id, manufacturer.toString(), owner.toString()) }
        }
    }
}
