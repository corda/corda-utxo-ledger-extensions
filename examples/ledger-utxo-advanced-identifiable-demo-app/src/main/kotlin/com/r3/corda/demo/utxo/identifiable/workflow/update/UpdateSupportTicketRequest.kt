package com.r3.corda.demo.utxo.identifiable.workflow.update

import com.r3.corda.ledger.utxo.identifiable.query.IdentifiableStateQueries
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicketStatus
import java.time.Instant

data class UpdateSupportTicketRequest(
    val id: String,
    val reporter: String,
    val status: String,
    val observers: Collection<String>
) {

    @Suspendable
    fun getInputState(utxoLedgerService: UtxoLedgerService): StateAndRef<SupportTicket> {
        return utxoLedgerService.query(IdentifiableStateQueries.GET_BY_IDS, StateAndRef::class.java)
            .setCreatedTimestampLimit(Instant.now()).setLimit(50)
            .setOffset(0)
            .setParameter("ids", listOf(id))
            .execute()
            .results
            .filterIsInstance<StateAndRef<SupportTicket>>()
            .single()
    }

    @Suspendable
    fun getOutputState(inputState: StateAndRef<SupportTicket>): SupportTicket {
        val status = SupportTicketStatus.valueOf(status)
        return inputState.state.contractState.copy(status = status, id = inputState.state.contractState.id ?: inputState.ref)
    }

    @Suspendable
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + reporter).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}
