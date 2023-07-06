package com.r3.corda.demo.utxo.fungible.workflow.mint

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import com.r3.corda.demo.utxo.fungible.contract.Token
import com.r3.corda.demo.utxo.fungible.workflow.getMemberX500Name
import java.math.BigDecimal

data class MintTokenResponse(val balance: Map<String, BigDecimal>) {
    internal companion object {

        @Suppress("UNCHECKED_CAST")
        fun fromTransaction(transaction: UtxoSignedTransaction, memberLookup: MemberLookup): MintTokenResponse {
            val balance = transaction
                .outputStateAndRefs
                .map { it.state.contractState }
                .filterIsInstance<Token>()
                .associate { memberLookup.getMemberX500Name(it.owner).toString() to it.quantity.value }

            return MintTokenResponse(balance)
        }
    }
}
