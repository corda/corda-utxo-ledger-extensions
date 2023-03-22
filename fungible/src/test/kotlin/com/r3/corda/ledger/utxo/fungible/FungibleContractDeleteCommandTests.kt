package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FungibleContractDeleteCommandTests : ContractTest() {

    private val state = ExampleFungibleStateA(ALICE_KEY, BOB_KEY, NumericDecimal.TEN)
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On fungible state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "At least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete]", exception.message
        )
    }

    @Test
    fun `On fungible state(s) deleting, at least one fungible state input must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractDeleteCommand.CONTRACT_RULE_INPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is equal)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addOutputState(state)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractDeleteCommand.CONTRACT_RULE_SUM, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is greater)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addOutputState(state)
            addOutputState(state)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractDeleteCommand.CONTRACT_RULE_SUM, exception.message)
    }
}