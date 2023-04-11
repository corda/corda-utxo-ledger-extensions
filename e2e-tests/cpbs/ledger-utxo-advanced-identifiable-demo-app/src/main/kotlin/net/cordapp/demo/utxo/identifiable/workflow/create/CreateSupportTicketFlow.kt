package net.cordapp.demo.utxo.identifiable.workflow.create

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.flows.InitiatingFlow
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import net.cordapp.demo.utxo.identifiable.workflow.AppLogger
import net.cordapp.demo.utxo.identifiable.workflow.addCreatedSupportTicket

@Suppress("unused")
class CreateSupportTicketFlow(
    private val ticket: SupportTicket,
    private val notary: MemberX500Name,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<CreateSupportTicketFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addCreatedSupportTicket(ticket, notary)
            .toSignedTransaction()

        logger.logFinalizingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "create-support-ticket-flow")
    @Suppress("unused")
    internal class Initiator : ClientStartableFlow {

        private companion object {
            val logger = AppLogger.create<Initiator>()
        }

        @CordaInject
        private lateinit var jsonMarshallingService: JsonMarshallingService

        @CordaInject
        private lateinit var memberLookup: MemberLookup

        @CordaInject
        private lateinit var notaryLookup: NotaryLookup

        @CordaInject
        private lateinit var flowMessaging: FlowMessaging

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, CreateSupportTicketRequest::class.java)
            logger.logMarshallingRequest(request)

            val ticket = request.getOutputState(memberLookup)
            val notary = request.getNotary(notaryLookup)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(CreateSupportTicketFlow(ticket, notary, sessions))

            return jsonMarshallingService.format(CreateSupportTicketResponse.fromTransaction(transaction))
        }
    }
}
