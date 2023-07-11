package com.r3.corda.demo.utxo.identifiable.workflow.update

import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.flows.InitiatedBy
import net.corda.v5.application.flows.ResponderFlow
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket
import com.r3.corda.demo.utxo.identifiable.workflow.AppLogger

@Suppress("unused")
class UpdateSupportTicketFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<UpdateSupportTicketFlowResponder>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logReceivingFinalizedTransaction()
        val result = utxoLedgerService.receiveFinality(session) { transaction ->
            val supportTicket = transaction.getOutputStates(SupportTicket::class.java).single()
            val myLedgerKeys = memberLookup.myInfo().ledgerKeys

            check(supportTicket.assignee !in myLedgerKeys) {
                "Updating a support ticket can only be initiated by the ticket assignee."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "update-support-ticket-flow")
    @Suppress("unused")
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(UpdateSupportTicketFlowResponder(session))
        }
    }
}
