package net.cordapp.demo.utxo.identifiable.workflow.delete

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction

data class DeleteSupportTicketResponse(val id: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): DeleteSupportTicketResponse {
            return DeleteSupportTicketResponse(transaction.inputStateRefs.single().toString())
        }
    }
}
