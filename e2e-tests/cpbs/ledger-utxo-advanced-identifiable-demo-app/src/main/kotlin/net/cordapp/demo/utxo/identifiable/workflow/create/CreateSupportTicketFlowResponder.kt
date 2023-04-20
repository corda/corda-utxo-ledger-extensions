package net.cordapp.demo.utxo.identifiable.workflow.create

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
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import net.cordapp.demo.utxo.identifiable.workflow.AppLogger

@Suppress("unused")
class CreateSupportTicketFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<CreateSupportTicketFlowResponder>()
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

            check(supportTicket.reporter !in myLedgerKeys) {
                "Creating a support ticket can only be initiated by the ticket reporter."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "create-support-ticket-flow")
    @Suppress("unused")
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(CreateSupportTicketFlowResponder(session))
        }
    }
}
