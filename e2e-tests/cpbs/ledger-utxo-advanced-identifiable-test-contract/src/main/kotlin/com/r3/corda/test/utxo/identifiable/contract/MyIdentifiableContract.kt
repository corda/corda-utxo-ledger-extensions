package com.r3.corda.test.utxo.identifiable.contract

import com.r3.corda.ledger.utxo.identifiable.IdentifiableContract
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCreateCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractDeleteCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractUpdateCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class MyIdentifiableContract : IdentifiableContract() {

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    override fun onVerify(transaction: UtxoLedgerTransaction) {

    }

    class Create : IdentifiableContractCreateCommand<MyIdentifiableState>() {

        override fun getContractStateType(): Class<MyIdentifiableState> {
            return MyIdentifiableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Update : IdentifiableContractUpdateCommand<MyIdentifiableState>() {

        override fun getContractStateType(): Class<MyIdentifiableState> {
            return MyIdentifiableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }

    class Delete : IdentifiableContractDeleteCommand<MyIdentifiableState>() {

        override fun getContractStateType(): Class<MyIdentifiableState> {
            return MyIdentifiableState::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {

        }
    }
}
