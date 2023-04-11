package net.cordapp.demo.utxo.fungible.workflow.mint

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.workflow.firstLedgerKey
import net.cordapp.demo.utxo.fungible.workflow.getMemberInfo
import net.cordapp.demo.utxo.fungible.workflow.getNotaryInfo
import java.math.BigDecimal

internal data class MintTokenRequest(
    val issuer: String,
    val owner: String,
    val quantity: BigDecimal,
    val notary: String,
    val observers: List<String>
) {
    @Suspendable
    internal fun getOutputToken(memberLookup: MemberLookup): Token {
        val issuer = memberLookup.getMemberInfo(issuer)
        val owner = memberLookup.getMemberInfo(owner)
        return Token(issuer.name, issuer.firstLedgerKey, owner.firstLedgerKey, NumericDecimal(quantity, 2))
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
