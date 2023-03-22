package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import net.corda.v5.crypto.SecureHash
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.text.MessageFormat
import kotlin.test.assertEquals

class FungibleContractUpdateCommandTests : ContractTest() {

    private val stateA = ExampleFungibleStateA(ALICE_KEY, BOB_KEY, NumericDecimal.TEN)
    private val stateB = ExampleFungibleStateB(ALICE_KEY, BOB_KEY, NumericDecimal.TEN)
    val hash = SecureHash.parse("SHA256:000000000000000000000000000000000000000000000000000000000000000B")
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        contract.verify(transaction)
    }

    @Test
    fun `On fungible state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateA)
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(
            "At least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete]", exception.message
        )
    }

    @Test
    fun `On fungible state(s) updating, at least one fungible state input must be consumed`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractUpdateCommand.CONTRACT_RULE_INPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, at least one fungible state must be created`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractUpdateCommand.CONTRACT_RULE_OUTPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateA)
            addOutputState(stateA.copy(quantity = NumericDecimal.ZERO))
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractUpdateCommand.CONTRACT_RULE_POSITIVE_QUANTITIES, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (quantity increase)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA.copy(quantity = NumericDecimal.ONE))
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractUpdateCommand.CONTRACT_RULE_SUM, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (quantity decrease)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateA.copy(quantity = NumericDecimal.ONE))
            addCommand(ExampleFungibleContract.Update())
        }

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleContractUpdateCommand.CONTRACT_RULE_SUM, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (hash changed)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateA.copy(identifierHash = hash))
            addCommand(ExampleFungibleContract.Update())
        }

        val expected = MessageFormat.format(
            FungibleContractUpdateCommand.CONTRACT_RULE_GROUP_SUM,
            stateA.javaClass.name,
            stateA.identifierHash
        )

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(expected, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (class changed)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateB.copy(identifierHash = stateA.identifierHash))
            addCommand(ExampleFungibleContract.Update())
        }

        val expected = MessageFormat.format(
            FungibleContractUpdateCommand.CONTRACT_RULE_GROUP_SUM,
            stateA.javaClass.name,
            stateA.identifierHash
        )

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(expected, exception.message)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (class and hash changed)`() {

        // Arrange
        val transaction = buildTransaction(NOTARY_PARTY) {
            addInputState(stateA)
            addOutputState(stateB)
            addCommand(ExampleFungibleContract.Update())
        }

        val expected = MessageFormat.format(
            FungibleContractUpdateCommand.CONTRACT_RULE_GROUP_SUM,
            stateA.javaClass.name,
            stateA.identifierHash
        )

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(expected, exception.message)
    }
}
