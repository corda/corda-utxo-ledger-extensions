package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.test.assertEquals

class FungibleContractCreateCommandTests : ContractTest() {

    private val state = ExampleFungibleStateA(ALICE_KEY, BOB_KEY, NumericDecimal(BigDecimal.TEN))
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(state)
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On fungible state(s) creating, the transaction should should include the Create command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
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
    fun `On fungible state(s) creating, zero fungible state inputs must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(state)
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractCreateCommand.CONTRACT_RULE_INPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) creating, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(state.copy(quantity = NumericDecimal(BigDecimal.ZERO)))
            addCommand(ExampleFungibleContract.Create())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractCreateCommand.CONTRACT_RULE_POSITIVE_QUANTITIES, exception.message)
    }
}
