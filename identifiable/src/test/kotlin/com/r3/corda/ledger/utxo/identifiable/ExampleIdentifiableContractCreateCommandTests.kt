package com.r3.corda.ledger.utxo.identifiable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIdentifiableContractCreateCommandTests : ContractTest() {

    private val state = ExampleIdentifiableState(aliceKey, bobKey, null)
    private val contract = ExampleIdentifiableContract()

    @Test
    fun `On identifiable state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addCommand(ExampleIdentifiableContract.Create())
        }.toLedgerTransaction()

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On identifiable state(s) creating, the transaction should include the Create command`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.identifiable.ExampleIdentifiableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand<? extends com.r3.corda.ledger.utxo.identifiable.IdentifiableState>' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }

    @Test
    fun `On identifiable state(s) creating, at least one identifiable state must be created`() {
        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleIdentifiableContract.Create())
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IdentifiableConstraints.CONTRACT_RULE_CREATE_OUTPUTS, exception.message)
    }
}
