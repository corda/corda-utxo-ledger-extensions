package com.r3.corda.demo.utxo.chainable.workflow.issue

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import com.r3.corda.demo.utxo.chainable.contract.Vehicle
import com.r3.corda.demo.utxo.chainable.workflow.firstLedgerKey
import com.r3.corda.demo.utxo.chainable.workflow.getMemberInfo
import com.r3.corda.demo.utxo.chainable.workflow.getNotaryInfo
import java.util.UUID

data class IssueVehicleRequest(
    val make: String,
    val model: String,
    val id: UUID,
    val manufacturer: String,
    val owner: String,
    val notary: String,
    val observers: List<String>
) {
    @Suspendable
    internal fun getOutputToken(memberLookup: MemberLookup): Vehicle {
        val manufacturer = memberLookup.getMemberInfo(manufacturer)
        val owner = memberLookup.getMemberInfo(owner)
        return Vehicle(make, model, id, manufacturer.firstLedgerKey, owner.firstLedgerKey, null)
    }

    @Suspendable
    internal fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + owner).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }

    @Suspendable
    internal fun getNotary(notaryLookup: NotaryLookup): MemberX500Name {
        return notaryLookup.getNotaryInfo(notary).name
    }
}
