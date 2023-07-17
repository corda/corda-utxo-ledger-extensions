package com.r3.corda.demo.utxo.identifiable.workflow.update

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.flows.InitiatingFlow
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket
import com.r3.corda.demo.utxo.identifiable.workflow.AppLogger
import com.r3.corda.demo.utxo.identifiable.workflow.addUpdatedSupportTicket

@Suppress("unused")
class UpdateSupportTicketFlow(
    private val oldSupportTicket: StateAndRef<SupportTicket>,
    private val newSupportTicket: SupportTicket,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<UpdateSupportTicketFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addUpdatedSupportTicket(oldSupportTicket, newSupportTicket)
            .toSignedTransaction()

        logger.logFinalizingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "update-support-ticket-flow")
    @Suppress("unused")
    class Initiator : ClientStartableFlow {

        private companion object {
            val logger = AppLogger.create<Initiator>()
        }

        @CordaInject
        private lateinit var utxoLedgerService: UtxoLedgerService

        @CordaInject
        private lateinit var jsonMarshallingService: JsonMarshallingService

        @CordaInject
        private lateinit var flowMessaging: FlowMessaging

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, UpdateSupportTicketRequest::class.java)
            logger.logMarshallingRequest(request)

            val oldSupportTicket = request.getInputState(utxoLedgerService)
            val newSupportTicket = request.getOutputState(oldSupportTicket)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(UpdateSupportTicketFlow(oldSupportTicket, newSupportTicket, sessions))

            return jsonMarshallingService.format(UpdateSupportTicketResponse.fromTransaction(transaction))
        }
    }
}
