package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import com.r3.corda.ledger.utxo.testing.randomStateRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractCreateCommandTests : ContractTest() {

    private val state = ExampleChainableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleChainableContract()

    @Test
    fun `On chainable state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
            addCommand(ExampleChainableContract.Create())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On chainable state(s) creating, the transaction should include the Create command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand<? extends com.r3.corda.ledger.utxo.chainable.ChainableState<?>>' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }

    @Test
    fun `On chainable state(s) creating, at least one chainable state must be created`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addCommand(ExampleChainableContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableConstraints.CONTRACT_RULE_CREATE_OUTPUTS, exception.message)
    }

    @Test
    fun `On chainable state(s) creating, the previous state pointer of every created chainable state must be null`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state.next(randomStateRef()))
            addCommand(ExampleChainableContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableConstraints.CONTRACT_RULE_CREATE_POINTERS, exception.message)
    }
}
