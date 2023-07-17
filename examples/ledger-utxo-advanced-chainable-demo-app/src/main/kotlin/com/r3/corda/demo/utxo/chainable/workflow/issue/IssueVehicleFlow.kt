package com.r3.corda.demo.utxo.chainable.workflow.issue

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
import com.r3.corda.demo.utxo.chainable.contract.Vehicle
import com.r3.corda.demo.utxo.chainable.workflow.AppLogger
import com.r3.corda.demo.utxo.chainable.workflow.addIssuedVehicle

@Suppress("unused")
class IssueVehicleFlow(
    private val vehicle: Vehicle,
    private val notary: MemberX500Name,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<IssueVehicleFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addIssuedVehicle(vehicle, notary)
            .toSignedTransaction()

        logger.logFinalizingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "issue-vehicle-flow")
    @Suppress("unused")
    class Initiator : ClientStartableFlow {

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
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, IssueVehicleRequest::class.java)
            logger.logMarshallingRequest(request)

            val vehicle = request.getOutputToken(memberLookup)
            val notary = request.getNotary(notaryLookup)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(IssueVehicleFlow(vehicle, notary, sessions))

            return jsonMarshallingService.format(IssueVehicleResponse.fromTransaction(transaction, memberLookup))
        }
    }
}
