package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FungibleContractDeleteCommandTests : ContractTest() {

    private val stateA = ExampleFungibleStateA(ALICE_KEY, BOB_KEY, NumericDecimal.TEN)
    private val stateB = ExampleFungibleStateB(ALICE_KEY, BOB_KEY, NumericDecimal.ONE)
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(stateA.copy(quantity = NumericDecimal.TEN))
            addInputState(stateB.copy(quantity =  NumericDecimal.TEN))
            addOutputState(stateA.copy(quantity = NumericDecimal.ONE))
            addOutputState(stateB.copy(quantity =  NumericDecimal.ONE))
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On fungible state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(stateA)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.fungible.ExampleFungibleContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }

    @Test
    fun `On fungible state(s) deleting, at least one fungible state input must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_DELETE_INPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is equal)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(stateA)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_DELETE_SUM, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is greater)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(stateA)
            addOutputState(stateA)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_DELETE_SUM, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of consumed states that are fungible with each other must be greater than the sum of the created states that are fungible with each other`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(stateA)
            addOutputState(stateB)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_DELETE_GROUP_SUM, exception.message)
    }
}
