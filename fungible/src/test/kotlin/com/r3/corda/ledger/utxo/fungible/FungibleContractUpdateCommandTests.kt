package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test

class FungibleContractUpdateCommandTests : ContractTest() {

    private val stateA = ExampleFungibleStateA(aliceKey, bobKey, NumericDecimal.TEN)
    private val stateB = ExampleFungibleStateB(aliceKey, bobKey, NumericDecimal.TEN)

    @Test
    fun `On fungible state(s) updating, the transaction should verify successfully`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertVerifies(transaction2)
    }

    @Test
    fun `On fungible state(s) updating, the transaction should include the Update command`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA)
        }

        // Assert
        assertFailsWith(
            transaction2,
            "On 'com.r3.corda.ledger.utxo.fungible.ExampleFungibleContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand<? extends com.r3.corda.ledger.utxo.fungible.FungibleState<?>>' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete]."
        )
    }

    @Test
    fun `On fungible state(s) updating, at least one fungible state input must be consumed`() {

        // Arrange
        val transaction = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertFailsWith(transaction, FungibleConstraints.CONTRACT_RULE_UPDATE_INPUTS)
    }

    @Test
    fun `On fungible state(s) updating, at least one fungible state must be created`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_UPDATE_OUTPUTS)
    }

    @Test
    fun `On fungible state(s) updating, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addOutputState(stateA.copy(quantity = NumericDecimal.ZERO))
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction3 = buildTransaction {
            val tx1OutputStateRef = transaction1.outputStateAndRefs.single()
            val tx2OutputState = transaction2.outputStateAndRefs.single().state.contractState
            addInputState(tx1OutputStateRef.ref)
            addOutputState(tx1OutputStateRef.state.contractState)
            addOutputState(tx2OutputState)
            addCommand(ExampleFungibleContract.Update())
        }
        // Assert
        assertFailsWith(transaction3, FungibleConstraints.CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (quantity increase)`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA.copy(quantity = NumericDecimal.ONE))
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_UPDATE_SUM)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states (quantity decrease)`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA.copy(quantity = NumericDecimal.ONE))
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_UPDATE_SUM)
    }

    @Test
    fun `On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Update())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateB)
            addCommand(ExampleFungibleContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_UPDATE_GROUP_SUM)
    }
}
