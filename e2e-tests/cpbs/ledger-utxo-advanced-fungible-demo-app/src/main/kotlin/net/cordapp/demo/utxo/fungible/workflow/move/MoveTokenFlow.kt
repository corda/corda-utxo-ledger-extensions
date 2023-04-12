package net.cordapp.demo.utxo.fungible.workflow.move

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
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.workflow.AppLogger
import net.cordapp.demo.utxo.fungible.workflow.addMoveTokens

@Suppress("unused")
class MoveTokenFlow(
    private val oldTokens: List<StateAndRef<Token>>,
    private val newTokens: List<Token>,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<MoveTokenFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addMoveTokens(oldTokens, newTokens)
            .toSignedTransaction()

        logger.logBuildingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "move-token-flow")
    @Suppress("unused")
    class Initiator : ClientStartableFlow {

        private companion object {
            val logger = AppLogger.create<MoveTokenFlow>()
        }

        @CordaInject
        private lateinit var jsonMarshallingService: JsonMarshallingService

        @CordaInject
        private lateinit var utxoLedgerService: UtxoLedgerService

        @CordaInject
        private lateinit var memberLookup: MemberLookup

        @CordaInject
        private lateinit var flowMessaging: FlowMessaging

        @CordaInject
        private lateinit var flowEngine: FlowEngine

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, MoveTokenRequest::class.java)
            logger.logMarshallingRequest(request)

            val tokenSelector = MoveTokenSelector(request, utxoLedgerService, memberLookup)

            val oldTokens = tokenSelector.getInputTokens()
            val newTokens = tokenSelector.getOutputTokens(oldTokens)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(MoveTokenFlow(oldTokens, newTokens, sessions))

            return jsonMarshallingService.format(MoveTokenResponse.fromTransaction(transaction, memberLookup))
        }
    }
}
