package com.r3.corda.ledger.utxo.identifiable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIdentifiableContractDeleteCommandTests : ContractTest() {

    private val state = ExampleIdentifiableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleIdentifiableContract()

    @Test
    fun `On identifiable state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addCommand(ExampleIdentifiableContract.Delete())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On identifiable state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.identifiable.ExampleIdentifiableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand<? extends com.r3.corda.ledger.utxo.identifiable.IdentifiableState>' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }

    @Test
    fun `On identifiable state(s) deleting, at least one identifiable state must be consumed`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addCommand(ExampleIdentifiableContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_DELETE_INPUTS, exception.message)
    }
}
