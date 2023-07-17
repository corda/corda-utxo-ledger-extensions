package com.r3.corda.demo.utxo.fungible.workflow.burn

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
import com.r3.corda.demo.utxo.fungible.contract.Token
import com.r3.corda.demo.utxo.fungible.workflow.AppLogger

@Suppress("unused")
class BurnTokenFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<BurnTokenFlowResponder>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logReceivingFinalizedTransaction()
        val result = utxoLedgerService.receiveFinality(session) { transaction ->
            val tokens = transaction.getInputStates(Token::class.java)
            val myLedgerKeys = memberLookup.myInfo().ledgerKeys

            check(tokens.none { it.owner in myLedgerKeys }) {
                "Token burning can only be initiated by the token owner."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "burn-token-flow")
    @Suppress("unused")
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(BurnTokenFlowResponder(session))
        }
    }
}
