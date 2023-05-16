package com.r3.corda.demo.utxo.identifiable.workflow.testing

import com.r3.corda.demo.utxo.identifiable.contract.testing.MyContractState
import com.r3.corda.demo.utxo.identifiable.contract.testing.MyIdentifiableContract
import com.r3.corda.demo.utxo.identifiable.contract.testing.MyIdentifiableState
import com.r3.corda.demo.utxo.identifiable.workflow.firstLedgerKey
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

class IdentifiableContractUpdateTestFlow(
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
            "CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY" -> {
                val stateRef = stateAndRefs.first().ref
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyIdentifiableState }
                    .map {
                        (it.state.contractState as MyIdentifiableState).copy(
                            id = stateRef
                        )
                    }
            }
            // TODO Currently broken fixed in CORE-13473
            "CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY MISSING_ID" -> {
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyIdentifiableState }
                    .let { stateAndRefs ->
                        val missingId = stateAndRefs.drop(1).map { it.state.contractState }
                        val hasId = stateAndRefs.take(1).map { (it.state.contractState as MyIdentifiableState).copy(id = it.ref) }
                        missingId + hasId
                    }

            }
            "VALID" -> {
                stateAndRefs.map { it.ref } to stateAndRefs
                    .filter { it.state.contractState is MyIdentifiableState }
                    .map {
                        (it.state.contractState as MyIdentifiableState).copy(
                            id = it.ref
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
            .addCommand(MyIdentifiableContract.Update())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}
