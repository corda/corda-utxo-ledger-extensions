package com.r3.corda.ledger.utxo.ownable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleOwnableContractTransferCommandTests : ContractTest() {

    private val state = ExampleOwnableState(BOB_KEY)
    private val contract = ExampleOwnableContract()

    @Test
    fun `On ownable state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addOutputState(state)
            addSignatory(BOB_KEY)
            addCommand(ExampleOwnableContract.Update)
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On ownable state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addOutputState(state)
            addSignatory(BOB_KEY)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.ownable.ExampleOwnableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.ownable.ExampleOwnableContract\$ExampleOwnableContractCommand' must be included in the transaction.\n" + "The permitted commands include [Update].",
            exception.message
        )
    }

    @Test
    fun `On ownable state(s) updating, the owner of every consumed ownable state must sign the transaction`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addOutputState(state)
            addCommand(ExampleOwnableContract.Update)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(OwnableConstraints.CONTRACT_RULE_UPDATE_SIGNATORIES, exception.message)
    }
}
