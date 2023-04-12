package net.cordapp.demo.utxo.identifiable.workflow.delete

import net.corda.v5.application.crypto.DigestService
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
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import net.cordapp.demo.utxo.identifiable.workflow.AppLogger
import net.cordapp.demo.utxo.identifiable.workflow.addDeletedSupportTicket

@Suppress("unused")
class DeleteSupportTicketFlow(
    private val supportTicket: StateAndRef<SupportTicket>,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<DeleteSupportTicketFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addDeletedSupportTicket(supportTicket)
            .toSignedTransaction()

        logger.logFinalizingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "delete-support-ticket-flow")
    @Suppress("unused")
    class Initiator : ClientStartableFlow {

        private companion object {
            val logger = AppLogger.create<Initiator>()
        }

        @CordaInject
        private lateinit var utxoLedgerService: UtxoLedgerService

        @CordaInject
        private lateinit var digestService: DigestService

        @CordaInject
        private lateinit var jsonMarshallingService: JsonMarshallingService

        @CordaInject
        private lateinit var flowMessaging: FlowMessaging

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, DeleteSupportTicketRequest::class.java)
            logger.logMarshallingRequest(request)

            val supportTicket = request.getInputState(utxoLedgerService, digestService)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(DeleteSupportTicketFlow(supportTicket, sessions))

            return jsonMarshallingService.format(DeleteSupportTicketResponse.fromTransaction(transaction))
        }
    }
}
