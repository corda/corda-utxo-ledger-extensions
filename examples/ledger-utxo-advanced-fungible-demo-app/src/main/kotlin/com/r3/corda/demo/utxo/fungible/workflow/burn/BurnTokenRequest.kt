package com.r3.corda.demo.utxo.fungible.workflow.burn

import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import java.math.BigDecimal

data class BurnTokenRequest(
    val issuer: String,
    val owner: String,
    val quantity: BigDecimal,
    val observers: Collection<String>
) {

    @Suspendable
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = (observers + issuer).map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}
