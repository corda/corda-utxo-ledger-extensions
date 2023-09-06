package com.r3.corda.ledger.utxo.ownable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleOwnableContractTransferCommandTests : ContractTest() {

    private val state = ExampleOwnableState(bobKey)
    private val contract = ExampleOwnableContract()

    @Test
    fun `On ownable state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleOwnableContract.Update)
        }
        val transaction2 = buildTransaction {
            val tx1StateRef = transaction1.outputStateAndRefs.single()
            addInputState(tx1StateRef.ref)
            addOutputState(tx1StateRef.state.contractState)
            addSignatories(bobKey)
            addCommand(ExampleOwnableContract.Update)
        }.toLedgerTransaction()

        // Act
        contract.verify(transaction2)
    }

    @Test
    fun `On ownable state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
        }
        val transaction2 = buildTransaction {
            val tx1StateRef = transaction1.outputStateAndRefs.single()
            addInputState(tx1StateRef.ref)
            addOutputState(tx1StateRef.state.contractState)
            addSignatories(bobKey)
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction2) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.ownable.ExampleOwnableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.ownable.ExampleOwnableContract\$ExampleOwnableContractCommand' must be included in the transaction.\n" + "The permitted commands include [Update].",
            exception.message
        )
    }

    @Test
    fun `On ownable state(s) updating, the owner of every consumed ownable state must sign the transaction`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleOwnableContract.Update)
        }
        val transaction2 = buildTransaction {
            val tx1StateRef = transaction1.outputStateAndRefs.single()
            addInputState(tx1StateRef.ref)
            addOutputState(tx1StateRef.state.contractState)
            addCommand(ExampleOwnableContract.Update)
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction2) }

        // Assert
        assertEquals(OwnableConstraints.CONTRACT_RULE_UPDATE_SIGNATORIES, exception.message)
    }
}
