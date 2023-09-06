package com.r3.corda.ledger.utxo.issuable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test

class ExampleIssuableContractCreateCommandTests : ContractTest() {

    private val state = ExampleIssuableState(aliceKey)
    private val contract = ExampleIssuableContract()

    @Test
    fun `On issuable state(s) creating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addSignatories(aliceKey)
            addCommand(ExampleIssuableContract.Create)
        }

        // Act & assert
        assertVerifies(transaction)
    }

    @Test
    fun `On issuable state(s) creating, the transaction should include the Create command`() {

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
    fun `On issuable state(s) creating, the issuer of every created issuable state must sign the transaction`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addCommand(ExampleIssuableContract.Create)
        }

        // Act & assert
        assertFailsWith(transaction, IssuableConstraints.CONTRACT_RULE_CREATE_SIGNATORIES)
    }
}
