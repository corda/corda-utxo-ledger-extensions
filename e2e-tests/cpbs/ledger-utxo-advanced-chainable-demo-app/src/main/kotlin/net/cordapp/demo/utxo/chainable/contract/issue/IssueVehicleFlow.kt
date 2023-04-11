package net.cordapp.demo.utxo.chainable.contract.issue

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
import net.cordapp.demo.utxo.chainable.contract.Vehicle
import net.cordapp.demo.utxo.chainable.contract.addIssuedVehicle

@Suppress("unused")
class IssueVehicleFlow(
    private val vehicle: Vehicle,
    private val notary: MemberX500Name,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addIssuedVehicle(vehicle, notary)
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "issue-vehicle-flow")
    @Suppress("unused")
    internal class Initiator : ClientStartableFlow {

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

        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, IssueVehicleRequest::class.java)

            val vehicle = request.getOutputToken(memberLookup)
            val notary = request.getNotary(notaryLookup)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(IssueVehicleFlow(vehicle, notary, sessions))

            return jsonMarshallingService.format(IssueVehicleResponse.fromTransaction(transaction))
        }
    }
}
