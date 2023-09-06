package com.r3.corda.ledger.utxo.issuable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleIssuableContractDeleteCommandTests : ContractTest() {

    private val state = ExampleIssuableState(aliceKey)
    private val contract = ExampleIssuableContract()

    @Test
    fun `On issuable state(s) deleting, the transaction should verify successfully`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleIssuableContract.Delete)
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addSignatories(aliceKey)
            addCommand(ExampleIssuableContract.Delete)
        }

        // Act & assert
        assertVerifies(transaction2)
    }

    @Test
    fun `On issuable state(s) deleting, the transaction should include the Delete command`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addSignatories(aliceKey)
        }

        // Act & assert
        assertFailsWith(
            transaction,
            "On 'com.r3.corda.ledger.utxo.issuable.ExampleIssuableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.issuable.ExampleIssuableContract\$ExampleIssuableContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Delete]."
        )
    }

    @Test
    fun `On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleIssuableContract.Delete)
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addCommand(ExampleIssuableContract.Delete)
        }

        // Act & assert
        assertFailsWith(transaction2, IssuableConstraints.CONTRACT_RULE_DELETE_SIGNATORIES)
    }
}
