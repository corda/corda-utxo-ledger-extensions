package com.r3.corda.test.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.NumericInteger
import com.r3.corda.test.utxo.fungible.contract.MyContractState
import com.r3.corda.test.utxo.fungible.contract.MyFungibleContract
import com.r3.corda.test.utxo.fungible.contract.MyFungibleState
import com.r3.corda.test.utxo.fungible.contract.MyFungibleStateA
import com.r3.corda.test.utxo.fungible.contract.MyFungibleStateB
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.math.BigInteger
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class FungibleContractUpdateTestFlow(
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
            "CONTRACT_RULE_UPDATE_INPUTS" -> {
                emptyList<StateRef>() to listOf(MyContractState(UUID.randomUUID()))
            }
            "CONTRACT_RULE_UPDATE_OUTPUTS" -> {
                stateAndRefs.map { it.ref } to emptyList()
            }
            "CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES" -> {
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(quantity = NumericInteger.ZERO),
                    fungibleStateBStateAndRef.copy()
                )
            }
            "CONTRACT_RULE_UPDATE_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(quantity = fungibleStateAStateAndRef.quantity + NumericInteger(BigInteger.valueOf(100))),
                    fungibleStateBStateAndRef.copy()
                )
            }
            // Groups fungible with each other must be equal.
            // Fungible state A has increased but it is not fungible with B so the check fails.
            "CONTRACT_RULE_UPDATE_GROUP_SUM" -> {
                val total = (fungibleStateAStateAndRef.quantity + fungibleStateBStateAndRef.quantity).unscaledValue.toLong()
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(quantity = fungibleStateAStateAndRef.quantity + NumericInteger.ONE),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf(total / 2))),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf((total / 2) - 1))),
                )
            }
            "VALID" -> {
                val total = (fungibleStateAStateAndRef.quantity + fungibleStateBStateAndRef.quantity).unscaledValue.toLong()
                stateAndRefs.map { it.ref } to listOf(
                    fungibleStateAStateAndRef.copy(),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf(total / 2))),
                    fungibleStateBStateAndRef.copy(quantity = NumericInteger(BigInteger.valueOf(total / 2))),
                )
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addInputStates(inputs)
            .addOutputStates(outputs)
            .addSignatories(key)
            .addCommand(MyFungibleContract.Update())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}
