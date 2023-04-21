package com.r3.corda.ledger.utxo.issuable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIssuableContractDeleteCommandTests : ContractTest() {

    private val state = ExampleIssuableState(ALICE_KEY)
    private val contract = ExampleIssuableContract()

    @Test
    fun `On issuable state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addSignatory(ALICE_KEY)
            addCommand(ExampleIssuableContract.Delete)
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On issuable state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addOutputState(state)
            addSignatory(ALICE_KEY)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.issuable.ExampleIssuableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.issuable.ExampleIssuableContract\$ExampleIssuableContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Delete].", exception.message
        )
    }

    @Test
    fun `On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_KEY, NOTARY_NAME) {
            addInputState(state)
            addCommand(ExampleIssuableContract.Delete)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(IssuableConstraints.CONTRACT_RULE_DELETE_SIGNATORIES, exception.message)
    }
}
