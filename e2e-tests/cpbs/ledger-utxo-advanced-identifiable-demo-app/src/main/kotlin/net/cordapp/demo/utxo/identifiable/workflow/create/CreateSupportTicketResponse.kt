package net.cordapp.demo.utxo.identifiable.workflow.create

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction

data class CreateSupportTicketResponse(val id: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): CreateSupportTicketResponse {
            return CreateSupportTicketResponse(transaction.outputStateAndRefs.single().ref.toString())
        }
    }
}
