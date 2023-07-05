package com.r3.corda.demo.utxo.fungible.workflow.testing

import com.r3.corda.demo.utxo.fungible.contract.testing.MyContractState
import com.r3.corda.demo.utxo.fungible.contract.testing.MyFungibleContract
import com.r3.corda.demo.utxo.fungible.contract.testing.MyFungibleStateA
import com.r3.corda.demo.utxo.fungible.contract.testing.MyFungibleStateB
import com.r3.corda.demo.utxo.fungible.workflow.firstLedgerKey
import com.r3.corda.ledger.utxo.fungible.NumericInteger
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
        val (inputs, outputs) = when (rule) {
            "CONTRACT_RULE_UPDATE_INPUTS" -> {
                emptyList<StateRef>() to listOf(MyContractState(UUID.randomUUID()))
            }
            "CONTRACT_RULE_UPDATE_OUTPUTS" -> {
                stateAndRefs.map { it.ref } to emptyList()
            }
            "CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger.ZERO,
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger.TEN,
                        owner = key
                    )
                )
            }
            "CONTRACT_RULE_UPDATE_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger(BigInteger.valueOf(100)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger.TEN,
                        owner = key
                    )
                )
            }
            "CONTRACT_RULE_UPDATE_GROUP_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger(BigInteger.valueOf(2)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(4)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(5)),
                        owner = key
                    ),
                )
            }
            "VALID" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger.ONE,
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(5)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(5)),
                        owner = key
                    ),
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