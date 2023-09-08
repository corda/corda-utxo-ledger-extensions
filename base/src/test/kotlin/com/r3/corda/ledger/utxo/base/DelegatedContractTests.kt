package com.r3.corda.ledger.utxo.base

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import net.corda.v5.ledger.utxo.ContractState
import net.corda.v5.ledger.utxo.VisibilityChecker
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.PublicKey

class DelegatedContractTests : ContractTest() {

    @Test
    fun `isVisible returns true when contract deems a state visible`() {
        MyDelegatedContract(isVisible = true).isVisible(createVisibleState(isVisible = false)) { false }
    }

    @Test
    fun `isVisible returns true when a state is a VisibleState and its isVisible returns true`() {
        MyDelegatedContract(isVisible = false).isVisible(createVisibleState(isVisible = true)) { false }
    }

    @Test
    fun `isVisible returns false when the contract deems a state not visible and the state is not a VisibleState`() {
        val state = ContractState { emptyList() }
        MyDelegatedContract(isVisible = false).isVisible(state) { false }
    }

    @Test
    fun `isVisible returns false when the contract deems a state not visible and the state is a VisibleState and its isVisible returns false`() {
        MyDelegatedContract(isVisible = false).isVisible(createVisibleState(isVisible = false)) { false }
    }

    @Test
    fun `verify calls onVerify from the concrete implementation of DelegatedContract`() {
        val transaction = buildTransaction {
            addCommand(MyVerifiableCommand.One())
        }

        assertVerifies(transaction)
    }

    @Test
    fun `verify throws an exception when the permitted command types is empty`() {
        val contract = MyDelegatedContract(isVisible = true, permittedCommands = emptyList())
        val transaction = buildTransaction {
            addCommand(MyVerifiableCommand.One())
        }.toLedgerTransaction()

        assertThrows<IllegalStateException> { contract.verify(transaction) }
    }

    @Test
    fun `verify throws an exception when none of the transaction's commands are permitted commands`() {
        val contract = MyDelegatedContract(isVisible = true)
        val transaction = buildTransaction {
            addCommand(MyOtherCommand())
        }.toLedgerTransaction()
        assertThrows<IllegalStateException> { contract.verify(transaction) }
    }

    @Test
    fun `verify calls verify on each permitted command in the transaction`() {
        val contract = MyDelegatedContract(isVisible = true)
        val commandOne = MyVerifiableCommand.One()
        val commandTwo = MyVerifiableCommand.Two()
        val transaction = buildTransaction {
            addCommand(commandOne)
            addCommand(commandTwo)
        }.toLedgerTransaction()
        contract.verify(transaction)
        assertTrue(commandOne.hasVerifyBeenCalled)
        assertTrue(commandTwo.hasVerifyBeenCalled)
    }

    @Test
    fun `verify throws an exception when a permitted command throws an exception`() {
        val contract = MyDelegatedContract(isVisible = true)
        val transaction = buildTransaction {
            addCommand(MyVerifiableCommand.One())
            addCommand(MyVerifiableCommand.Two())
            addCommand(MyVerifiableCommand.Three())
        }.toLedgerTransaction()
        assertThrows<IllegalArgumentException> { contract.verify(transaction) }
    }

    private fun createVisibleState(isVisible: Boolean): VisibleState {
        return object : VisibleState {
            override fun getParticipants(): List<PublicKey> = emptyList()

            override fun isVisible(checker: VisibilityChecker): Boolean {
                return isVisible
            }
        }
    }

    private class MyDelegatedContract(
        private val isVisible: Boolean,
        private val onVerifyCallback: () -> Unit = {},
        private val permittedCommands: List<Class<out MyVerifiableCommand>> = listOf(
            MyVerifiableCommand.One::class.java,
            MyVerifiableCommand.Two::class.java,
            MyVerifiableCommand.Three::class.java
        )
    ) : DelegatedContract<MyVerifiableCommand>() {

        override fun getPermittedCommandTypes(): List<Class<out MyVerifiableCommand>> {
            return permittedCommands
        }

        override fun isVisible(state: ContractState, checker: VisibilityChecker): Boolean {
            return isVisible
        }

        override fun onVerify(transaction: UtxoLedgerTransaction) {
            onVerifyCallback()
        }
    }

    private interface MyVerifiableCommand : VerifiableCommand {

        class One(var hasVerifyBeenCalled: Boolean = false) : MyVerifiableCommand {
            override fun verify(transaction: UtxoLedgerTransaction) {
                hasVerifyBeenCalled = true
                // pass
            }
        }

        class Two(var hasVerifyBeenCalled: Boolean = false) : MyVerifiableCommand {
            override fun verify(transaction: UtxoLedgerTransaction) {
                hasVerifyBeenCalled = true
                // pass
            }
        }

        class Three(var hasVerifyBeenCalled: Boolean = false) : MyVerifiableCommand {
            override fun verify(transaction: UtxoLedgerTransaction) {
                hasVerifyBeenCalled = true
                throw IllegalArgumentException("failed")
            }
        }
    }

    private class MyOtherCommand : VerifiableCommand {
        override fun verify(transaction: UtxoLedgerTransaction) {
            // pass
        }
    }
}
