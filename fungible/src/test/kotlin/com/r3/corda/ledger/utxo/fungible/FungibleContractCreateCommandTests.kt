package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FungibleContractCreateCommandTests : ContractTest() {

    private val state = ExampleFungibleStateA(aliceKey, bobKey, NumericDecimal.TEN)
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addCommand(ExampleFungibleContract.Create())
        }

        // Assert
        assertVerifies(transaction)
    }

    @Test
    fun `On fungible state(s) creating, the transaction should should include the Create command`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
        }

        // Assert
        assertFailsWith(transaction, "On 'com.r3.corda.ledger.utxo.fungible.ExampleFungibleContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand<? extends com.r3.corda.ledger.utxo.fungible.FungibleState<?>>' must be included in the transaction.\n" +
                "The permitted commands include [Create, Update, Delete].")
    }

    @Test
    fun `On fungible state(s) creating, at least one fungible state must be created`() {

        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleFungibleContract.Create())
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_CREATE_OUTPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) creating, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state.copy(quantity = NumericDecimal.ZERO))
            addCommand(ExampleFungibleContract.Create())
        }

        // Assert
        assertFailsWith(transaction, FungibleConstraints.CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES)
    }
}
