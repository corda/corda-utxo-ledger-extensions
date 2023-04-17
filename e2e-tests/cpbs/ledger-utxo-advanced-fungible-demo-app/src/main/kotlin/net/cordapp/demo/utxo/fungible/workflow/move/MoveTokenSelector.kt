package net.cordapp.demo.utxo.fungible.workflow.move

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.membership.MemberInfo
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.workflow.firstLedgerKey
import net.cordapp.demo.utxo.fungible.workflow.getAvailableTokens
import net.cordapp.demo.utxo.fungible.workflow.getMemberInfo
import net.cordapp.demo.utxo.fungible.workflow.sum

class MoveTokenSelector(
    private val request: MoveTokenRequest,
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
        val inputTokens = mutableListOf<StateAndRef<Token>>()
        val targetQuantity = NumericDecimal(request.shares.values.sum(), 2)
        val availableTokens = utxoLedgerService.getAvailableTokens(
            issuerMemberInfo.ledgerKeys,
            ownerMemberInfo.ledgerKeys,
            targetQuantity,
            digestService
        )
        var remainder = targetQuantity

        for (availableToken in availableTokens) {
            if (remainder <= NumericDecimal.ZERO.setScale(2)) break
            inputTokens.add(availableToken)
            remainder -= availableToken.state.contractState.quantity
        }

        return inputTokens
    }

    @Suspendable
    fun getOutputTokens(inputTokens: List<StateAndRef<Token>>): List<Token> {
        val outputTokens = mutableListOf<Token>()
        val targetQuantity = NumericDecimal(request.shares.values.sum(), 2)
        val changeQuantity = inputTokens.map { it.state.contractState.quantity }.sum() - targetQuantity

        for (share in request.shares) {
            val quantity = NumericDecimal(share.value, 2)
            val owner = memberLookup.getMemberInfo(share.key)
            outputTokens.add(
                Token(
                    issuerMemberInfo.name,
                    issuerMemberInfo.firstLedgerKey,
                    owner.firstLedgerKey,
                    quantity
                )
            )
        }

        if (changeQuantity > NumericDecimal.ZERO.setScale(2)) {
            outputTokens.add(
                Token(
                    issuerMemberInfo.name,
                    issuerMemberInfo.firstLedgerKey,
                    ownerMemberInfo.firstLedgerKey,
                    changeQuantity
                )
            )
        }

        return outputTokens
    }
}
