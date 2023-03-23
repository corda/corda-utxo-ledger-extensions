package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ExampleChainableContractDisallowedCommandTests : ContractTest() {

    private val contract = ExampleChainableContract()

    @Test
    fun `Attempting to build a transaction that uses DisallowedCommand should fail`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addCommand(ExampleChainableContract.DisallowedCommand())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete].", exception.message
        )
    }
}
