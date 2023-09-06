package com.r3.corda.ledger.utxo.chainable

import com.r3.corda.ledger.utxo.testing.ContractTest
import com.r3.corda.ledger.utxo.testing.buildTransaction
import org.junit.jupiter.api.Test

class ExampleChainableContractUpdateCommandTests : ContractTest() {

    private val state = ExampleChainableState(aliceKey, bobKey, null)
    private val anotherState = ExampleChainableState(aliceKey, bobKey, null)

    @Test
    fun `On chainable state(s) updating, the transaction should verify successfully`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        val transaction2 = buildTransaction {
            val outputRef = transaction1.outputStateAndRefs.single().ref
            addInputState(outputRef)
            addOutputState(state.next(outputRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertVerifies(transaction2)
    }

    @Test
    fun `On chainable state(s) updating, the transaction should include the Update command`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
        }

        val transaction2 = buildTransaction {
            val outputRef = transaction1.outputStateAndRefs.single().ref
            addInputState(outputRef)
            addOutputState(state.next(outputRef))
        }

        // Assert
        assertFailsWith(transaction2, "On 'com.r3.corda.ledger.utxo.chainable.ExampleChainableContract' contract executing, at least one command of type 'com.r3.corda.ledger.utxo.chainable.ChainableContractCommand<? extends com.r3.corda.ledger.utxo.chainable.ChainableState<?>>' must be included in the transaction.\n" +
                "The permitted commands include [Create, Update, Delete].")
    }

    @Test
    fun `On chainable state(s) updating, at least one chainable state must be consumed`() {
        // Arrange
        val transaction = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction, ChainableConstraints.CONTRACT_RULE_UPDATE_INPUTS)
    }

    @Test
    fun `On chainable state(s) updating, at least one chainable state must be created`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        val transaction2 = buildTransaction {
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, ChainableConstraints.CONTRACT_RULE_UPDATE_OUTPUTS)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must not be null`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }

        val transaction2 = buildTransaction {
            val outputStateAndRef = transaction1.outputStateAndRefs.single()
            addInputState(outputStateAndRef.ref)
            addOutputState(outputStateAndRef.state.contractState)
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, ChainableConstraints.CONTRACT_RULE_UPDATE_POINTERS)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (unconsumed pointer)`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction2 =  buildTransaction {
            addOutputState(anotherState)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction3 = buildTransaction {
            val inputStateRef = transaction2.outputStateAndRefs.single().ref
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(state.next(inputStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction3, ChainableConstraints.CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (mismatched pointer)`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction2 = buildTransaction {
            addOutputState(anotherState)
            addCommand(ExampleChainableContract.Update())
        }

        val transaction3 = buildTransaction {
            val invalidPointerStateRef = transaction2.outputStateAndRefs.single().ref
            addInputState(transaction1.outputStateAndRefs.single().ref)
            addOutputState(state.next(invalidPointerStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction3, ChainableConstraints.CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (merging pointers)`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction2 = buildTransaction {
            addOutputState(anotherState)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction3 = buildTransaction {
            val inputStateRef1 = transaction1.outputStateAndRefs.single().ref
            val inputStateRef2 = transaction2.outputStateAndRefs.single().ref
            addInputState(inputStateRef1)
            addInputState(inputStateRef2)
            addOutputState(state.next(inputStateRef1))
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction3, ChainableConstraints.CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS)
    }

    @Test
    fun `On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively (splitting pointers)`() {
        // Arrange
        val transaction1 = buildTransaction {
            addOutputState(state)
            addCommand(ExampleChainableContract.Update())
        }
        val transaction2 = buildTransaction {
            val inputStateRef = transaction1.outputStateAndRefs.single().ref
            addInputState(inputStateRef)
            addOutputState(state.next(inputStateRef))
            addOutputState(state.next(inputStateRef))
            addCommand(ExampleChainableContract.Update())
        }

        // Assert
        assertFailsWith(transaction2, ChainableConstraints.CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS)
    }
}
