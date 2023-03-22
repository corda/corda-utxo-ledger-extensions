package com.r3.corda.ledger.utxo.identifiable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import com.r3.corda.ledger.utxo.testing.randomStateRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIdentifiableContractTests : ContractTest() {

    private val state = ExampleIdentifiableState(ALICE_KEY, BOB_KEY, null)
    private val contract = ExampleIdentifiableContract()

    @Test
    fun `On identifiable state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(state)
            addCommand(ExampleIdentifiableContract.Create())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On identifiable state(s) creating, the transaction should include the Create command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(state)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "At least one command of type 'com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand' must be included in the transaction.\n" + "The permitted commands include [Create, Update, Delete]",
            exception.message
        )
    }

    @Test
    fun `On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively (initial state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val stateRef1 = randomStateRef()
            addInputState(state, stateRef1, NOTARY_PARTY, null)
            addOutputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableContract.CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY, exception.message)
    }

    @Test
    fun `On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively (evolved state)`() {
        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            val stateRef1 = randomStateRef()
            addInputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addOutputState(state.copy(id = stateRef1))
            addCommand(ExampleIdentifiableContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableContract.CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY, exception.message)
    }
}