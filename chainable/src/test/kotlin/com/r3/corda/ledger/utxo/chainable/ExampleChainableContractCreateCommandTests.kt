package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractCreateCommandTests : ContractTest() {

    private val state = ExampleChainableState(aliceKey, bobKey, null)
    private val anotherState = ExampleChainableState(aliceKey, bobKey, null)
    private val contract = ExampleChainableContract()

    @Test
    fun `On chainable state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Create())
        }

        // Act
        assertVerifies(transaction)
    }

    @Test
    fun `On chainable state(s) creating, the transaction should include the Create command`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
        }

        assertFailsWith(transaction, "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand<? extends com.r3.corda.ledger.utxo.chainable.ChainableState<?>>' must be included in the transaction.\n" +
                "The permitted commands include [Create, Update, Delete].")
    }

    @Test
    fun `On chainable state(s) creating, at least one chainable state must be created`() {
        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleChainableContract.Create())
        }.toLedgerTransaction()

        // Act
        // this test couldn't be asserted using assertFailsWith(transaction, ChainableConstraints.CONTRACT_RULE_CREATE_OUTPUTS)
        // due to assertFailsWith() skips verifying the states in:
        // https://github.com/corda/corda5-contract-testing/blob/main/contract-testing/src/main/java/com/r3/corda/ledger/utxo/testing/ContractTest.java#L143
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

      // Assert
        assertEquals(ChainableConstraints.CONTRACT_RULE_CREATE_OUTPUTS, exception.message)
    }

    @Test
    fun `On chainable state(s) creating, the previous state pointer of every created chainable state must be null`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(anotherState)
            addCommand(ExampleChainableContract.Create())
        }
        val transaction2 = buildTransaction {
            addOutputState(state.next(transaction1.outputStateAndRefs.single().ref))
            addCommand(ExampleChainableContract.Create())
        }

        assertFailsWith(transaction2, ChainableConstraints.CONTRACT_RULE_CREATE_POINTERS)
    }
}
