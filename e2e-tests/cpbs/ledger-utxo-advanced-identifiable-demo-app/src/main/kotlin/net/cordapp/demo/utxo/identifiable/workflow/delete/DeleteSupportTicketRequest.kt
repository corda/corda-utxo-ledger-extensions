package net.cordapp.demo.utxo.identifiable.workflow.delete

import com.r3.corda.ledger.utxo.identifiable.query.IdentifiableStateQueries
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import java.time.Instant

data class DeleteSupportTicketRequest(val id: String, val assignee: String, val observers: Collection<String>) {

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
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + assignee).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}
