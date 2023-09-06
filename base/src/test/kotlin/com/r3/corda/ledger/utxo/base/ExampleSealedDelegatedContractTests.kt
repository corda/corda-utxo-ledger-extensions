package com.r3.corda.ledger.utxo.base

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ExampleSealedDelegatedContractTests : ContractTest() {

    private val contract = ExampleSealedDelegatedContract()

    @Test
    fun `Permitted should be considered a permitted command`() {

        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleSealedDelegatedContract.ExampleSealedContractCommand.Permitted)
        }.toLedgerTransaction()

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `NotPermitted should be considered a permitted command`() {

        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleSealedDelegatedContract.ExampleSealedContractCommand.NotPermitted())
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "On 'com.r3.corda.ledger.utxo.base.ExampleSealedDelegatedContractTests\$ExampleSealedDelegatedContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.base.ExampleSealedDelegatedContractTests\$ExampleSealedDelegatedContract\$ExampleSealedContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [ExampleSealedContractCommand].", exception.message
        )
    }

    class ExampleSealedDelegatedContract : DelegatedContract<ExampleSealedDelegatedContract.ExampleSealedContractCommand>() {

        sealed interface ExampleSealedContractCommand : VerifiableCommand {
            object Permitted : ExampleSealedContractCommand {
                override fun verify(transaction: UtxoLedgerTransaction) {
                }
            }

            class NotPermitted : VerifiableCommand {
                override fun verify(transaction: UtxoLedgerTransaction) {
                }
            }
        }

        override fun getPermittedCommandTypes(): List<Class<out ExampleSealedContractCommand>> {
            return listOf(ExampleSealedContractCommand::class.java)
        }
    }
}
