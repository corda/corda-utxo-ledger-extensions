package com.r3.corda.demo.utxo.fungible.workflow.burn

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.membership.MemberInfo
import com.r3.corda.demo.utxo.fungible.contract.Token
import com.r3.corda.demo.utxo.fungible.workflow.firstLedgerKey
import com.r3.corda.demo.utxo.fungible.workflow.getAvailableTokens
import com.r3.corda.demo.utxo.fungible.workflow.getMemberInfo
import com.r3.corda.demo.utxo.fungible.workflow.sum

class BurnTokenSelector(
    private val request: BurnTokenRequest,
    private val utxoLedgerService: UtxoLedgerService,
    private val memberLookup: MemberLookup,
    private val digestService: DigestService
) {

    private val issuerMemberInfo: MemberInfo
        get() = memberLookup.getMemberInfo(request.issuer)

    private val ownerMemberInfo: MemberInfo
        get() = memberLookup.getMemberInfo(request.owner)

    @Suspendable
    fun getInputTokens(): List<StateAndRef<Token>> {
        val targetQuantity = NumericDecimal(request.quantity, 2)
        val availableTokens = utxoLedgerService.getAvailableTokens(
            issuerMemberInfo.ledgerKeys,
            ownerMemberInfo.ledgerKeys,
            targetQuantity,
            digestService
        )
        val inputTokens = mutableListOf<StateAndRef<Token>>()
        var remainder = targetQuantity

        for (availableToken in availableTokens) {
            if (remainder <= NumericDecimal.ZERO.setScale(2)) break
            inputTokens.add(availableToken)
            remainder -= availableToken.state.contractState.quantity
        }

        return inputTokens
    }

    @Suspendable
    fun getChangeToken(inputTokens: List<StateAndRef<Token>>): Token? {
        val targetQuantity = NumericDecimal(request.quantity, 2)
        val changeQuantity = inputTokens.map { it.state.contractState.quantity }.sum() - targetQuantity

        return if (changeQuantity == NumericDecimal.ZERO.setScale(2)) null
        else Token(
            issuerMemberInfo.name,
            issuerMemberInfo.firstLedgerKey,
            ownerMemberInfo.firstLedgerKey,
            changeQuantity
        )
    }
}
