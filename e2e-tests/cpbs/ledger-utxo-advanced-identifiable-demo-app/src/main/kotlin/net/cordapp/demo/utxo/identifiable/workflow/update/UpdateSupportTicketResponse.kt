package net.cordapp.demo.utxo.identifiable.workflow.update

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction

data class UpdateSupportTicketResponse(val id: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): UpdateSupportTicketResponse {
            return UpdateSupportTicketResponse(transaction.outputStateAndRefs.single().ref.toString())
        }
    }
}
