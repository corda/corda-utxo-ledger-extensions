package com.r3.corda.ledger.utxo.identifiable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import com.r3.corda.ledger.utxo.testing.randomStateRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIdentifiableContractUpdateCommandTests : ContractTest() {

    private val state = ExampleIdentifiableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleIdentifiableContract()

    @Test
    fun `On identifiable state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state, stateRef1, NOTARY_KEY, NOTARY_NAME, null)
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On identifiable state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state, stateRef1, NOTARY_KEY, NOTARY_NAME, null)
            addOutputState(state.copy(id = stateRef1))
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
    fun `On identifiable state(s) updating, at least one identifiable state must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_INPUTS, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, at least one identifiable state must be created`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_OUTPUTS, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, only one identifiable state with a matching identifier must be consumed for every created identifiable state with a non-null identifier`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addOutputState(state.copy(id = randomStateRef()))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_IDENTIFIERS, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null (initial state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state, stateRef1, NOTARY_KEY, NOTARY_NAME, null)
            addOutputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_OUTPUT_IDENTIFIER_EXCLUSIVITY, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null (evolved state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_OUTPUT_IDENTIFIER_EXCLUSIVITY, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, every consumed identifiable state's identifier must appear only once when the identifier is not null (initial state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state, stateRef1, NOTARY_KEY, NOTARY_NAME, null)
            addInputState(state, stateRef1, NOTARY_KEY, NOTARY_NAME, null)
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_INPUT_IDENTIFIER_EXCLUSIVITY, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, every consumed identifiable state's identifier must appear only once when the identifier is not null (evolved state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            val stateRef1 = randomStateRef()
            addInputState(state.copy(id = stateRef1))
            addInputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_UPDATE_INPUT_IDENTIFIER_EXCLUSIVITY, exception.message)
    }
}
