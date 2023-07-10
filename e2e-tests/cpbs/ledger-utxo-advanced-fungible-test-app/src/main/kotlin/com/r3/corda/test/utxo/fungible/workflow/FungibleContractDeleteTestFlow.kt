package com.r3.corda.test.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.FungibleState
import com.r3.corda.ledger.utxo.fungible.NumericInteger
import com.r3.corda.test.utxo.fungible.contract.MyFungibleContract
import com.r3.corda.test.utxo.fungible.contract.MyFungibleStateA
import com.r3.corda.test.utxo.fungible.contract.MyFungibleStateB
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.math.BigInteger
import java.time.Instant
import java.time.temporal.ChronoUnit

class FungibleContractDeleteTestFlow(
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
        val states = stateAndRefs.map { it.state.contractState }
        val fungibleStateAStateAndRef = states.filterIsInstance<MyFungibleStateA>().single()
        val fungibleStateBStateAndRef = states.filterIsInstance<MyFungibleStateB>().single()
        val (inputs, outputs) = when (rule) {
            "CONTRACT_RULE_DELETE_INPUTS" -> {
                stateAndRefs
                    .filter { it.state.contractState !is FungibleState<*> }
                    .map { it.ref } to emptyList()
            }
            "CONTRACT_RULE_DELETE_POSITIVE_QUANTITIES" -> {
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(quantity = NumericInteger.ZERO),
                    fungibleStateBStateAndRef.copy()
                )
            }
            "CONTRACT_RULE_DELETE_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(),
                    fungibleStateBStateAndRef.copy()
                )
            }
            // Must be more inputs (deleted) states than outputs (created) per group.
            // Fungible state A has increased compared to the input of state A so fails the check.
            "CONTRACT_RULE_DELETE_GROUP_SUM" -> {
                val total = (fungibleStateAStateAndRef.quantity + fungibleStateBStateAndRef.quantity).unscaledValue.toLong()
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(quantity = fungibleStateAStateAndRef.quantity + NumericInteger.ONE),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf(total / 2 - 1))),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf((total / 2) - 1))),
                )
            }
            "VALID" -> {
                stateAndRefs.map { it.ref } to emptyList()
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addInputStates(inputs)
            .addOutputStates(outputs)
            .addSignatories(key)
            .addCommand(MyFungibleContract.Delete())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}
