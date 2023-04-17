package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FungibleContractCreateCommandTests : ContractTest() {

    private val state = ExampleFungibleStateA(ALICE_KEY, BOB_KEY, NumericDecimal.TEN)
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On fungible state(s) creating, the transaction should should include the Create command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
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
    fun `On fungible state(s) creating, at least one fungible state must be created`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_CREATE_OUTPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) creating, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state.copy(quantity = NumericDecimal.ZERO))
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES, exception.message)
    }
}
