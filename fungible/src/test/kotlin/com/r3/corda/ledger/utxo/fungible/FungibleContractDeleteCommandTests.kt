package com.r3.corda.ledger.utxo.fungible

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class FungibleContractDeleteCommandTests : ContractTest() {

    private val stateA = ExampleFungibleStateA(aliceKey, bobKey, NumericDecimal.TEN)
    private val stateB = ExampleFungibleStateB(aliceKey, bobKey, NumericDecimal.ONE)
    private val contract = ExampleFungibleContract()

    @Test
    fun `On fungible state(s) deleting, the transaction should verify successfully`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA.copy(quantity =  NumericDecimal.TEN))
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction2 = buildTransaction {
            addOutputState(stateB.copy(quantity =  NumericDecimal.TEN))
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction3 = buildTransaction {
            val stateRefA = transaction1.outputStateAndRefs.single().ref
            val stateRefB = transaction2.outputStateAndRefs.single().ref
            addInputState(stateRefA)
            addInputState(stateRefB)
            addOutputState(stateA.copy(quantity = NumericDecimal.ONE))
            addOutputState(stateB.copy(quantity =  NumericDecimal.ONE))
            addCommand(ExampleFungibleContract.Delete())
        }

        //Assert
        assertVerifies(transaction3)
    }

    @Test
    fun `On fungible state(s) deleting, the transaction should include the Delete command`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
        }

        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
        }

        // Assert
        assertFailsWith(
            transaction2,
            "On 'com.r3.corda.ledger.utxo.fungible.ExampleFungibleContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.fungible.FungibleContractCommand<? extends com.r3.corda.ledger.utxo.fungible.FungibleState<?>>' must be included in the transaction.\n" +
                    "The permitted commands include [Create, Update, Delete]."
        )
    }

    @Test
    fun `On fungible state(s) deleting, at least one fungible state input must be consumed`() {

        // Arrange
        val transaction = buildTransaction {
            addCommand(ExampleFungibleContract.Delete())
        }.toLedgerTransaction()

        // Act
        val exception = assertThrows<IllegalStateException> { contract.verify(transaction) }

        // Assert
        assertEquals(FungibleConstraints.CONTRACT_RULE_DELETE_INPUTS, exception.message)
    }

    @Test
    fun `On fungible state(s) deleting, the quantity of every created fungible state must be greater than zero`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA.copy(quantity = NumericDecimal.ZERO))
            addCommand(ExampleFungibleContract.Delete())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_DELETE_POSITIVE_QUANTITIES)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is equal)`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_DELETE_SUM)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of the absolute values of the consumed states must be greater than the sum of the absolute values of the created states (quantity is greater)`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateA)
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_DELETE_SUM)
    }

    @Test
    fun `On fungible state(s) deleting, the sum of consumed states that are fungible with each other must be greater than the sum of the created states that are fungible with each other`() {

        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(stateA)
            addCommand(ExampleFungibleContract.Delete())
        }
        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(stateB)
            addCommand(ExampleFungibleContract.Delete())
        }

        // Assert
        assertFailsWith(transaction2, FungibleConstraints.CONTRACT_RULE_DELETE_GROUP_SUM)
    }
}
