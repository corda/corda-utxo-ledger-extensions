package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractDeleteCommandTests : ContractTest() {

    private val state = ExampleChainableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleChainableContract()

    @Test
    fun `On chainable state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addCommand(ExampleChainableContract.Delete())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On chainable state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }

    @Test
    fun `On chainable state(s) deleting, at least one chainable state must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addCommand(ExampleChainableContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableConstraints.CONTRACT_RULE_DELETE_INPUTS, exception.message)
    }
}
