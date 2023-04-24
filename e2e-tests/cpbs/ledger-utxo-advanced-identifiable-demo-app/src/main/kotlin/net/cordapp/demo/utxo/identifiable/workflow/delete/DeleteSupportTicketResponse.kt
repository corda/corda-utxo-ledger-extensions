package net.cordapp.demo.utxo.identifiable.workflow.delete

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket

data class DeleteSupportTicketResponse(val id: String, val title: String) {
    internal companion object {
        @Suspendable
        fun fromTransaction(transaction: UtxoSignedTransaction): DeleteSupportTicketResponse {
            val state = transaction.toLedgerTransaction().inputStateAndRefs.single().state.contractState as SupportTicket
            return DeleteSupportTicketResponse(state.id!!.toString(), state.title)
        }
    }
}
