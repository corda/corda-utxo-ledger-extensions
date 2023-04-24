package com.r3.corda.demo.utxo.identifiable.contract

import com.r3.corda.ledger.utxo.identifiable.IdentifiableContract
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCreateCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractDeleteCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractUpdateCommand
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

class SupportTicketContract : IdentifiableContract() {

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : IdentifiableContractCreateCommand<SupportTicket>() {
        internal companion object {
            const val CONTRACT_RULE_INPUTS =
                "On support ticket creating, zero support ticket states must be consumed."

            const val CONTRACT_RULE_OUTPUTS =
                "On support ticket creating, only one support ticket state must be created."

            const val CONTRACT_RULE_STATUS =
                "On support ticket creating, the support ticket status must be TODO."

            const val CONTRACT_RULE_SIGNERS =
                "On support ticket creating, the support ticket reporter must sign the transaction."
        }

        override fun getContractStateType(): Class<SupportTicket> {
            return SupportTicket::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(SupportTicket::class.java)
            val outputs = transaction.getOutputStates(SupportTicket::class.java)

            check(inputs.isEmpty()) { CONTRACT_RULE_INPUTS }
            check(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val output = outputs.single()

            check(output.status == SupportTicketStatus.TODO) { CONTRACT_RULE_STATUS }
            check(output.reporter in transaction.signatories) { CONTRACT_RULE_SIGNERS }
        }
    }

    class Update : IdentifiableContractUpdateCommand<SupportTicket>() {
        internal companion object {
            const val CONTRACT_RULE_INPUTS =
                "On support ticket updating, only one support ticket state must be consumed."

            const val CONTRACT_RULE_OUTPUTS =
                "On support ticket updating, only one support ticket state must be created."

            const val CONTRACT_RULE_STATUS =
                "On support ticket updating, the support ticket status must transition from TODO to OPEN, or OPEN to DONE."

            const val CONTRACT_RULE_SIGNERS =
                "On support ticket updating, the support ticket assignee must sign the transaction."
        }

        override fun getContractStateType(): Class<SupportTicket> {
            return SupportTicket::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(SupportTicket::class.java)
            val outputs = transaction.getOutputStates(SupportTicket::class.java)

            check(inputs.size == 1) { CONTRACT_RULE_INPUTS }
            check(outputs.size == 1) { CONTRACT_RULE_OUTPUTS }

            val input = inputs.single()
            val output = outputs.single()

            check(input.status.canTransitionTo(output.status)) { CONTRACT_RULE_STATUS }
            check(output.assignee in transaction.signatories) { CONTRACT_RULE_SIGNERS }
        }
    }

    class Delete : IdentifiableContractDeleteCommand<SupportTicket>() {
        internal companion object {
            const val CONTRACT_RULE_INPUTS =
                "On support ticket deleting, only one support ticket state must be consumed."

            const val CONTRACT_RULE_OUTPUTS =
                "On support ticket deleting, zero support ticket states must be created."

            const val CONTRACT_RULE_STATUS =
                "On support ticket deleting, the support ticket status must be DONE."

            const val CONTRACT_RULE_SIGNERS =
                "On support ticket deleting, the support ticket reporter must sign the transaction."
        }

        override fun getContractStateType(): Class<SupportTicket> {
            return SupportTicket::class.java
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            val inputs = transaction.getInputStates(SupportTicket::class.java)
            val outputs = transaction.getOutputStates(SupportTicket::class.java)

            check(inputs.size == 1) { CONTRACT_RULE_INPUTS }
            check(outputs.isEmpty()) { CONTRACT_RULE_OUTPUTS }

            val input = inputs.single()

            check(input.status == SupportTicketStatus.DONE) { CONTRACT_RULE_STATUS }
            check(input.reporter in transaction.signatories) { CONTRACT_RULE_SIGNERS }
        }
    }
}
