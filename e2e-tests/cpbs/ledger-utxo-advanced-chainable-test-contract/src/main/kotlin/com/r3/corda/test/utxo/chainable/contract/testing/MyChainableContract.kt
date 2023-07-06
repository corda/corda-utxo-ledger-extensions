package com.r3.corda.test.utxo.chainable.contract.testing

import com.r3.corda.ledger.utxo.chainable.ChainableContract
import com.r3.corda.ledger.utxo.chainable.ChainableContractCommand
import com.r3.corda.ledger.utxo.chainable.ChainableContractCreateCommand
import com.r3.corda.ledger.utxo.chainable.ChainableContractDeleteCommand
import com.r3.corda.ledger.utxo.chainable.ChainableContractUpdateCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class MyChainableContract : ChainableContract() {

    override fun getPermittedCommandTypes(): List<Class<out ChainableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    override fun onVerify(transaction: UtxoLedgerTransaction) {

    }

    class Create : ChainableContractCreateCommand<MyChainableState>() {

        override fun getContractStateType(): Class<MyChainableState> {
            return MyChainableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Update : ChainableContractUpdateCommand<MyChainableState>() {

        override fun getContractStateType(): Class<MyChainableState> {
            return MyChainableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Delete : ChainableContractDeleteCommand<MyChainableState>() {

        override fun getContractStateType(): Class<MyChainableState> {
            return MyChainableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }
}
