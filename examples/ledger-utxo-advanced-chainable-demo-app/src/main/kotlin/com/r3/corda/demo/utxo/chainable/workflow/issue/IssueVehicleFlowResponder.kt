package com.r3.corda.demo.utxo.chainable.workflow.issue

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
import com.r3.corda.demo.utxo.chainable.contract.Vehicle
import com.r3.corda.demo.utxo.chainable.workflow.AppLogger

@Suppress("unused")
class IssueVehicleFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<IssueVehicleFlowResponder>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logReceivingFinalizedTransaction()
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
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(IssueVehicleFlowResponder(session))
        }
    }
}
