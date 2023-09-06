package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractDeleteCommandTests : ContractTest() {

    private val state = ExampleChainableState(aliceKey, bobKey, null)
    private val contract = ExampleChainableContract()

    @Test
    fun `On chainable state(s) deleting, the transaction should verify successfully`() {
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Delete())
        }

        // Arrange
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addCommand(ExampleChainableContract.Delete())
        }

        // Assert
        assertVerifies(transaction2)
    }

    @Test
    fun `On chainable state(s) deleting, the transaction should include the Delete command`() {
        val transaction1 = buildTransaction {
            addOutputState(state)
        }

        // Arrange
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
        }

        // Assert
        assertFailsWith(transaction2, "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand<? extends com.r3.corda.ledger.utxo.chainable.ChainableState<?>>' must be included in the transaction.\n" +
                "The permitted commands include [Create, Update, Delete].")
    }

    @Test
    fun `On chainable state(s) deleting, at least one chainable state must be consumed`() {

        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleChainableContract.Delete())
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableConstraints.CONTRACT_RULE_DELETE_INPUTS, exception.message)
    }
}
