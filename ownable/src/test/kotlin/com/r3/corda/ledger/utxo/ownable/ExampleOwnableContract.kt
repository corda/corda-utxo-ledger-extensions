package com.r3.corda.ledger.utxo.ownable

import com.r3.corda.ledger.utxo.base.ContractStateType
import com.r3.corda.ledger.utxo.base.DelegatedContract
import com.r3.corda.ledger.utxo.base.VerifiableCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class ExampleOwnableContract : DelegatedContract<ExampleOwnableContract.ExampleOwnableContractCommand>() {

    override fun getPermittedCommandTypes(): List<Class<out ExampleOwnableContractCommand>> {
        return listOf(Update::class.java)
    }

    sealed interface ExampleOwnableContractCommand : VerifiableCommand, ContractStateType<ExampleOwnableState>

    object Update : ExampleOwnableContractCommand {

        override fun getContractStateType(): Class<ExampleOwnableState> {
            return ExampleOwnableState::class.java
        }

        override fun verify(transaction: UtxoLedgerTransaction) {
            OwnableConstraints.verifyUpdate(transaction, contractStateType)
        }
    }
}
