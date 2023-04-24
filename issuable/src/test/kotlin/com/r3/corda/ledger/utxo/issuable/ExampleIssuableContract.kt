package com.r3.corda.ledger.utxo.issuable

import com.r3.corda.ledger.utxo.base.ContractStateType
import com.r3.corda.ledger.utxo.base.DelegatedContract
import com.r3.corda.ledger.utxo.base.VerifiableCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class ExampleIssuableContract : DelegatedContract<ExampleIssuableContract.ExampleIssuableContractCommand>() {

    override fun getPermittedCommandTypes(): List<Class<out ExampleIssuableContractCommand>> {
        return listOf(Create::class.java, Delete::class.java)
    }

    sealed interface ExampleIssuableContractCommand : VerifiableCommand, ContractStateType<ExampleIssuableState>


    object Create : ExampleIssuableContractCommand {
        override fun getContractStateType(): Class<ExampleIssuableState> {
            return ExampleIssuableState::class.java
        }

        override fun verify(transaction: UtxoLedgerTransaction) {
            IssuableConstraints.verifyCreate(transaction, contractStateType)
        }
    }

    object Delete : ExampleIssuableContractCommand {
        override fun getContractStateType(): Class<ExampleIssuableState> {
            return ExampleIssuableState::class.java
        }

        override fun verify(transaction: UtxoLedgerTransaction) {
            IssuableConstraints.verifyDelete(transaction, contractStateType)
        }
    }
}
