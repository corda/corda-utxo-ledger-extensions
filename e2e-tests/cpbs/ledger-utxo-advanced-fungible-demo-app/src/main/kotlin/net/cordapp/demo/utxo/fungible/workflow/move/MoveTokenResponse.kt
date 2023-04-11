package net.cordapp.demo.utxo.fungible.workflow.move


import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.cordapp.demo.utxo.fungible.contract.Token
import net.cordapp.demo.utxo.fungible.workflow.getMemberX500Name
import java.math.BigDecimal

data class MoveTokenResponse(val balance: Map<String, BigDecimal>) {
    internal companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromTransaction(transaction: UtxoSignedTransaction, memberLookup: MemberLookup): MoveTokenResponse {
            val balance = transaction
                .outputStateAndRefs
                .map { it.state.contractState }
                .filterIsInstance<Token>()
                .associate { memberLookup.getMemberX500Name(it.owner).toString() to it.quantity.value }

            return MoveTokenResponse(balance)
        }
    }
}