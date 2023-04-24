package com.r3.corda.demo.utxo.fungible.workflow.burn

import net.corda.v5.application.crypto.DigestService
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
import com.r3.corda.demo.utxo.fungible.contract.Token
import com.r3.corda.demo.utxo.fungible.workflow.AppLogger
import com.r3.corda.demo.utxo.fungible.workflow.addBurnTokens

@Suppress("unused")
class BurnTokenFlow(
    private val oldTokens: List<StateAndRef<Token>>,
    private val changeToken: Token?,
    private val sessions: List<FlowSession>
) : SubFlow<UtxoSignedTransaction> {

    private companion object {
        val logger = AppLogger.create<BurnTokenFlow>()
    }

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): UtxoSignedTransaction {
        logger.logBuildingTransaction()
        val transaction = utxoLedgerService
            .createTransactionBuilder()
            .addBurnTokens(oldTokens, changeToken)
            .toSignedTransaction()

        logger.logFinalizingTransaction()
        return utxoLedgerService.finalize(transaction, sessions).transaction
    }

    @InitiatingFlow(protocol = "burn-token-flow")
    @Suppress("unused")
    class Initiator : ClientStartableFlow {

        private companion object {
            val logger = AppLogger.create<Initiator>()
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

        @CordaInject
        private lateinit var digestService: DigestService

        @Suspendable
        override fun call(requestBody: ClientRequestBody): String {
            val request = requestBody.getRequestBodyAs(jsonMarshallingService, BurnTokenRequest::class.java)
            logger.logMarshallingRequest(request)

            val tokenSelector = BurnTokenSelector(request, utxoLedgerService, memberLookup, digestService)

            val oldTokens = tokenSelector.getInputTokens()
            val changeToken = tokenSelector.getChangeToken(oldTokens)
            val sessions = request.getFlowSessions(flowMessaging)

            val transaction = flowEngine.subFlow(BurnTokenFlow(oldTokens, changeToken, sessions))

            return jsonMarshallingService.format(BurnTokenResponse.fromTransaction(transaction))
        }
    }
}
