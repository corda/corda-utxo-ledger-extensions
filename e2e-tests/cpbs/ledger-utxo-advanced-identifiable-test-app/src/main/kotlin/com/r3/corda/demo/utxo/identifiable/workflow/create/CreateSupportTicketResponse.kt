package com.r3.corda.demo.utxo.identifiable.workflow.create

import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket

data class CreateSupportTicketResponse(val id: String, val title: String) {
    internal companion object {
        fun fromTransaction(transaction: UtxoSignedTransaction): CreateSupportTicketResponse {
            val stateAndRef = transaction.outputStateAndRefs.single()
            return CreateSupportTicketResponse(stateAndRef.ref.toString(), (stateAndRef.state.contractState as SupportTicket).title)
        }
    }
}
