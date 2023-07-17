package com.r3.corda.demo.utxo.identifiable.workflow.create

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket
import com.r3.corda.demo.utxo.identifiable.workflow.firstLedgerKey
import com.r3.corda.demo.utxo.identifiable.workflow.getMemberInfo
import com.r3.corda.demo.utxo.identifiable.workflow.getNotaryInfo

data class CreateSupportTicketRequest(
    val title: String,
    val description: String,
    val reporter: String,
    val assignee: String,
    val notary: String,
    val observers: Collection<String>
) {

    @Suspendable
    fun getOutputState(memberLookup: MemberLookup): SupportTicket {
        val reporter = memberLookup.getMemberInfo(reporter).firstLedgerKey
        val assignee = memberLookup.getMemberInfo(assignee).firstLedgerKey
        return SupportTicket(title, description, reporter, assignee)
    }

    @Suspendable
    fun getNotary(notaryLookup: NotaryLookup): MemberX500Name {
        return notaryLookup.getNotaryInfo(notary).name
    }

    @Suspendable
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + assignee).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}
