package com.r3.corda.demo.utxo.fungible.workflow.burn

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.fungible.contract.Token
import com.r3.corda.demo.utxo.fungible.workflow.sum
import java.math.BigDecimal

data class BurnTokenResponse(val quantities: Collection<BigDecimal>, val burned: BigDecimal, val change: BigDecimal) {
    internal companion object {
        @Suspendable
        fun fromTransaction(transaction: UtxoSignedTransaction): BurnTokenResponse {
            val ledgerTransaction = transaction.toLedgerTransaction()

            val quantities = ledgerTransaction
                .getInputStateAndRefs(Token::class.java)
                .map { it.state.contractState.quantity.value }

            val change = ledgerTransaction
                .getOutputStateAndRefs(Token::class.java)
                .map { it.state.contractState.quantity }
                .sum().value

            val burned = quantities.sum() - change

            return BurnTokenResponse(quantities, burned, change)
        }
    }
}
