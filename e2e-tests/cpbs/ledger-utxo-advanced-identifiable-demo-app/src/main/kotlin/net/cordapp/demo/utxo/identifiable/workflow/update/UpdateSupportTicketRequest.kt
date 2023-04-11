package net.cordapp.demo.utxo.identifiable.workflow.update

import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import net.cordapp.demo.utxo.identifiable.contract.SupportTicketStatus

data class UpdateSupportTicketRequest(
    val id: String,
    val reporter: String,
    val status: String,
    val observers: Collection<String>
) {

    @Suspendable
    fun getInputState(utxoLedgerService: UtxoLedgerService, digestService: DigestService): StateAndRef<SupportTicket> {
        val stateRef = StateRef.parse(id, digestService)
        return utxoLedgerService.findUnconsumedStatesByType(SupportTicket::class.java)
            .single { it.ref == stateRef || it.state.contractState.id == stateRef }
    }

    @Suspendable
    fun getOutputState(inputState: StateAndRef<SupportTicket>): SupportTicket {
        val status = SupportTicketStatus.valueOf(status)
        return inputState.state.contractState.copy(status = status)
    }

    @Suspendable
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + reporter).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}