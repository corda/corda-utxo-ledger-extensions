package net.cordapp.demo.utxo.chainable.contract.transfer

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
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.chainable.contract.Vehicle
import net.cordapp.demo.utxo.chainable.contract.addTransferredVehicle

@Suppress("unused")
class TransferVehicleFlow(
    private val oldVehicle: StateAndRef<Vehicle>,
    private val newVehicle: Vehicle,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addTransferredVehicle(oldVehicle, newVehicle)
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "update-vehicle-flow")
    @Suppress("unused")
    internal class Initiator : ClientStartableFlow {

        @CordaInject
        private lateinit var utxoLedgerService: UtxoLedgerService

        @CordaInject
        private lateinit var jsonMarshallingService: JsonMarshallingService

        @CordaInject
        private lateinit var memberLookup: MemberLookup

        @CordaInject
        private lateinit var flowMessaging: FlowMessaging

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, TransferVehicleRequest::class.java)

            val oldVehicle = request.getInputState(utxoLedgerService)
            val newSupportTicket = request.getOutputState(oldVehicle, memberLookup)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(TransferVehicleFlow(oldVehicle, newSupportTicket, sessions))

            return jsonMarshallingService.format(TransferVehicleResponse.fromTransaction(transaction))
        }
    }
}
