package com.r3.corda.demo.utxo.chainable.workflow.testing

import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableContract
import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableState
import com.r3.corda.demo.utxo.chainable.contract.testing.MyContractState
import com.r3.corda.demo.utxo.chainable.workflow.firstLedgerKey
import com.r3.corda.ledger.utxo.base.StaticPointer
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ChainableContractUpdateTestFlow(
    private val rule: String,
    private val stateAndRefs: List<StateAndRef<*>>
) : SubFlow<List<StateAndRef<*>>> {

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): List<StateAndRef<*>> {
        val key = memberLookup.myInfo().firstLedgerKey
        val (inputs, outputs) = when (rule) {
            "CONTRACT_RULE_UPDATE_INPUTS" -> {
                emptyList<StateRef>() to listOf(MyContractState(UUID.randomUUID()))
            }
            "CONTRACT_RULE_UPDATE_OUTPUTS" -> {
                stateAndRefs.map { it.ref } to emptyList()
            }
            "CONTRACT_RULE_UPDATE_POINTERS" -> {
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyChainableState }
                    .map { (it.state.contractState as MyChainableState).copy(previousStatePointer = null) }
            }
            "CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS" -> {
                val stateRef = stateAndRefs.first().ref
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyChainableState }
                    .map {
                        (it.state.contractState as MyChainableState).copy(
                            previousStatePointer = StaticPointer(
                                stateRef,
                                MyChainableState::class.java
                            )
                        )
                    }
            }
            "VALID" -> {
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyChainableState }
                    .map {
                        (it.state.contractState as MyChainableState).copy(
                            previousStatePointer = StaticPointer(
                                it.ref,
                                MyChainableState::class.java
                            )
                        )
                    }
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addInputStates(inputs)
            .addOutputStates(outputs)
            .addSignatories(key)
            .addCommand(MyChainableContract.Update())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}
