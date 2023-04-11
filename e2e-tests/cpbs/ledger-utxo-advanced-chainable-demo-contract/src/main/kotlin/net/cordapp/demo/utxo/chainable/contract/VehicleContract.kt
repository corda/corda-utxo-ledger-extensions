package net.cordapp.demo.utxo.chainable.contract

import com.r3.corda.ledger.utxo.chainable.ChainableContract
import com.r3.corda.ledger.utxo.chainable.ChainableContractCommand
import com.r3.corda.ledger.utxo.chainable.ChainableContractCreateCommand
import com.r3.corda.ledger.utxo.chainable.ChainableContractUpdateCommand
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class VehicleContract : ChainableContract() {

    internal companion object {
        const val CONTRACT_RULE_COMMANDS =
            "On vehicle contract executing, only one permitted command must be present in the transaction."
    }

    override fun getPermittedCommandTypes(): List<Class<out ChainableContractCommand>> {
        return listOf(Issue::class.java, Transfer::class.java)
    }

    override fun onVerify(transaction: UtxoLedgerTransaction) {
        val commands = transaction.getCommands(VehicleContractCommand::class.java)
        check(commands.size == 1) { CONTRACT_RULE_COMMANDS }
    }

    private interface VehicleContractCommand : Command

    class Issue : ChainableContractCreateCommand(), VehicleContractCommand {

        internal companion object {
            const val CONTRACT_RULE_INPUTS =
                "On vehicle issuing, zero vehicle states must be consumed."

            const val CONTRACT_RULE_OUTPUTS =
                "On vehicle issuing, only one vehicle state must be created."

            const val CONTRACT_RULE_SIGNATORIES =
                "On vehicle issuing, the vehicle manufacturer must sign the transaction."
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(Vehicle::class.java)
            val outputs = transaction.getOutputStates(Vehicle::class.java)

            check(inputs.isEmpty()) { CONTRACT_RULE_INPUTS }
            check(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val output = outputs.single()

            check(output.manufacturer in transaction.signatories) { CONTRACT_RULE_SIGNATORIES }
        }
    }

    class Transfer : ChainableContractUpdateCommand(), VehicleContractCommand {

        internal companion object {
            const val CONTRACT_RULE_INPUTS =
                "On vehicle transferring, only one vehicle state must be consumed."

            const val CONTRACT_RULE_OUTPUTS =
                "On vehicle transferring, only one vehicle state must be created."

            const val CONTRACT_RULE_SIGNATORIES =
                "On vehicle transferring, the vehicle owner must sign the transaction."
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(Vehicle::class.java)
            val outputs = transaction.getOutputStates(Vehicle::class.java)

            check(inputs.size == 1) { CONTRACT_RULE_INPUTS }
            check(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val input = inputs.single()

            check(input.owner in transaction.signatories) { CONTRACT_RULE_SIGNATORIES }
        }
    }
}