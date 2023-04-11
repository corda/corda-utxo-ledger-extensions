package net.cordapp.demo.utxo.chainable.contract.issue

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
import net.cordapp.demo.utxo.chainable.contract.Vehicle

@Suppress("unused")
class IssueVehicleFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        val result = utxoLedgerService.receiveFinality(session) { transaction ->
            val tokens = transaction.getOutputStates(Vehicle::class.java)
            val myLedgerKeys = memberLookup.myInfo().ledgerKeys

            check(tokens.none { it.manufacturer in myLedgerKeys }) {
                "Vehicle issuance can only be initiated by the vehicle manufacturer."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "issue-vehicle-flow")
    internal class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(IssueVehicleFlowResponder(session))
        }
    }
}
