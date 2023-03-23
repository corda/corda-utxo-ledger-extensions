package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.ContractTestUtils
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractUpdateCommandTests : ContractTest() {

    private val state = ExampleChainableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleChainableContract()

    @Test
    fun `On chainable state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef = ContractTestUtils.createRandomStateRef()
            addInputState(state, inputStateRef, NOTARY_PARTY, null)
            addOutputState(state.next(inputStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On chainable state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef = ContractTestUtils.createRandomStateRef()
            addInputState(state, inputStateRef, NOTARY_PARTY, null)
            addOutputState(state.next(inputStateRef))
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
    fun `On chainable state(s) updating, at least one chainable state must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_INPUTS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, at least one chainable state must be created`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_OUTPUTS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must not be null`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_POINTERS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (unconsumed pointer)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef = ContractTestUtils.createRandomStateRef()
            addInputState(state)
            addOutputState(state.next(inputStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_EXCLUSIVE_POINTERS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (mismatched pointer)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef = ContractTestUtils.createRandomStateRef()
            val invalidPointerStateRef = ContractTestUtils.createRandomStateRef()
            addInputState(state, inputStateRef, NOTARY_PARTY, null)
            addOutputState(state.next(invalidPointerStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_EXCLUSIVE_POINTERS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (merging pointers)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef1 = ContractTestUtils.createRandomStateRef()
            val inputStateRef2 = ContractTestUtils.createRandomStateRef()
            addInputState(state, inputStateRef1, NOTARY_PARTY, null)
            addInputState(state, inputStateRef2, NOTARY_PARTY, null)
            addOutputState(state.next(inputStateRef1))
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_EXCLUSIVE_POINTERS, exception.message)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (splitting pointers)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val inputStateRef = ContractTestUtils.createRandomStateRef()
            addInputState(state, inputStateRef, NOTARY_PARTY, null)
            addOutputState(state.next(inputStateRef))
            addOutputState(state.next(inputStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(ChainableContractUpdateCommand.CONTRACT_RULE_EXCLUSIVE_POINTERS, exception.message)
    }
}
