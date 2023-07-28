package com.r3.corda.demo.utxo.fungible.contract

import com.r3.corda.ledger.utxo.fungible.FungibleContract
import com.r3.corda.ledger.utxo.fungible.FungibleContractCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractCreateCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractDeleteCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractUpdateCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class TokenContract : FungibleContract() {

    override fun getPermittedCommandTypes(): List<Class<out FungibleContractCommand<*>>> {
        return listOf(Mint::class.java, Move::class.java, Burn::class.java)
    }

    class Mint : FungibleContractCreateCommand<Token>() {

        companion object {
            const val CONTRACT_RULE_SIGNATORIES = "On token(s) minting, the issuer must sign the transaction."
        }

        override fun getContractStateType(): Class<Token> {
            return Token::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val outputs = transaction.getInputStates(Token::class.java)
            check(outputs.all { it.issuer in transaction.signatories }) { CONTRACT_RULE_SIGNATORIES }
        }
    }

    class Move : FungibleContractUpdateCommand<Token>() {

        companion object {
            const val CONTRACT_RULE_SIGNATORIES = "On token(s) moving, the owner must sign the transaction."
        }

        override fun getContractStateType(): Class<Token> {
            return Token::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(Token::class.java)
            check(inputs.all { it.owner in transaction.signatories }) { CONTRACT_RULE_SIGNATORIES }
        }
    }

    class Burn : FungibleContractDeleteCommand<Token>() {

        companion object {
            const val CONTRACT_RULE_SIGNATORIES = "On token(s) burning, the issuer and owner must sign the transaction."
        }

        override fun getContractStateType(): Class<Token> {
            return Token::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(Token::class.java)
            val signingKeys = inputs.flatMap { listOf(it.issuer, it.owner) }
            check(signingKeys.all { it in transaction.signatories }) { CONTRACT_RULE_SIGNATORIES }
        }
    }
}
