package net.cordapp.demo.utxo.fungible.workflow.mint

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
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.workflow.AppLogger

@Suppress("unused")
class MintTokenFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<MintTokenFlowResponder>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logReceivingFinalizedTransaction()
        val result = utxoLedgerService.receiveFinality(session) { transaction ->
            val tokens = transaction.getOutputStates(Token::class.java)
            val myLedgerKeys = memberLookup.myInfo().ledgerKeys

            check(tokens.none { it.issuer in myLedgerKeys }) {
                "Token minting can only be initiated by the token issuer."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "mint-token-flow")
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(MintTokenFlowResponder(session))
        }
    }
}
