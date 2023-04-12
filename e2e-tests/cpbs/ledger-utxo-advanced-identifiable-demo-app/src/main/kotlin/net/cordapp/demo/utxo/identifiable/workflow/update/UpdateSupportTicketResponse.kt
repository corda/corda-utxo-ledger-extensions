package net.cordapp.demo.utxo.identifiable.workflow.update

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket

data class UpdateSupportTicketResponse(val id: String, val title: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): UpdateSupportTicketResponse {
            val stateAndRef = transaction.outputStateAndRefs.single()
            return UpdateSupportTicketResponse(stateAndRef.ref.toString(), (stateAndRef.state.contractState as SupportTicket).title)
        }
    }
}
