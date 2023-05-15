package com.r3.corda.demo.utxo.chainable.workflow.testing

import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableContract
import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableState
import com.r3.corda.demo.utxo.chainable.contract.testing.MyContractState
import com.r3.corda.demo.utxo.chainable.workflow.firstLedgerKey
import com.r3.corda.ledger.utxo.base.StaticPointer
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.crypto.DigestAlgorithmName
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.ContractState
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ChainableContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        when (request.command) {
            "CREATE" -> flowEngine.subFlow(ChainableContractCreateTestFlow(request.rule))
            "UPDATE" -> {
                val outputs = flowEngine.subFlow(ChainableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(ChainableContractUpdateTestFlow(request.rule, outputs))
            }
            "DELETE" -> {
                val outputs = flowEngine.subFlow(ChainableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(ChainableContractDeleteTestFlow(request.rule, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

class ChainableContractCreateTestFlow(private val rule: String) : SubFlow<List<StateAndRef<*>>> {

    @CordaInject
    private lateinit var digestService: DigestService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): List<StateAndRef<*>> {
        val key = memberLookup.myInfo().firstLedgerKey
        val outputs = when (rule) {
            "CONTRACT_RULE_CREATE_OUTPUTS" -> listOf(MyContractState(UUID.randomUUID()))
            "CONTRACT_RULE_CREATE_POINTERS" -> {
                listOf(
                    MyChainableState(
                        UUID.randomUUID(),
                        key,
                        StaticPointer(
                            StateRef(digestService.hash(byteArrayOf(1, 2, 3, 4), DigestAlgorithmName.SHA2_256), 0),
                            MyChainableState::class.java
                        )
                    ),
                    MyChainableState(UUID.randomUUID(), key, null)
                )
            }
            "VALID" -> {
                listOf(
                    MyChainableState(UUID.randomUUID(), memberLookup.myInfo().firstLedgerKey, null),
                    MyChainableState(UUID.randomUUID(), memberLookup.myInfo().firstLedgerKey, null),
                    MyContractState(UUID.randomUUID())
                )
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addOutputStates(outputs)
            .addSignatories(key)
            .addCommand(MyChainableContract.Create())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}

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

class ChainableContractDeleteTestFlow(
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
        val inputs = when (rule) {
            "CONTRACT_RULE_DELETE_INPUTS" -> {
                stateAndRefs
                    .filter { it.state.contractState !is MyChainableState }
                    .map { it.ref }
            }
            "VALID" -> {
                stateAndRefs.map { it.ref }
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addInputStates(inputs)
            .addSignatories(key)
            .addCommand(MyChainableContract.Delete())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}

private class Request(val command: String, val rule: String)
