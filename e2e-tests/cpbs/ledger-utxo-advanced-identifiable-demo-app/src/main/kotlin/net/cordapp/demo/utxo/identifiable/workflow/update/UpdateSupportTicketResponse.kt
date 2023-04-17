package net.cordapp.demo.utxo.identifiable.workflow.update

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket

data class UpdateSupportTicketResponse(val id: String, val title: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): UpdateSupportTicketResponse {
            val state = transaction.outputStateAndRefs.single().state.contractState as SupportTicket
            return UpdateSupportTicketResponse(state.id!!.toString(), state.title)
        }
    }
}
