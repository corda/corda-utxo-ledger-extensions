package com.r3.corda.demo.utxo.fungible.contract.testing

import com.r3.corda.ledger.utxo.fungible.FungibleContract
import com.r3.corda.ledger.utxo.fungible.FungibleContractCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractCreateCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractDeleteCommand
import com.r3.corda.ledger.utxo.fungible.FungibleContractUpdateCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class MyFungibleContract : FungibleContract() {

    override fun getPermittedCommandTypes(): List<Class<out FungibleContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    override fun onVerify(transaction: UtxoLedgerTransaction) {

    }

    class Create : FungibleContractCreateCommand<MyFungibleState>() {

        override fun getContractStateType(): Class<MyFungibleState> {
            return MyFungibleState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Update : FungibleContractUpdateCommand<MyFungibleState>() {

        override fun getContractStateType(): Class<MyFungibleState> {
            return MyFungibleState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Delete : FungibleContractDeleteCommand<MyFungibleState>() {

        override fun getContractStateType(): Class<MyFungibleState> {
            return MyFungibleState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }
}
