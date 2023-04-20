package net.cordapp.demo.utxo.fungible.workflow.move

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
class MoveTokenFlowResponder(private val session: FlowSession) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<MoveTokenFlowResponder>()
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
                "Token moving can only be initiated by the token owner."
            }
        }

        return result.transaction
    }

    @InitiatedBy(protocol = "move-token-flow")
    @Suppress("unused")
    class Responder : ResponderFlow {

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(session: FlowSession) {
            flowEngine.subFlow(MoveTokenFlowResponder(session))
        }
    }
}
