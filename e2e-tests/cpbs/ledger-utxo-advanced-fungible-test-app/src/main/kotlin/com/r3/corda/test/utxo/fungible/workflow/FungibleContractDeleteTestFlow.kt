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
        val (inputs, outputs) = when (rule) {
            "CONTRACT_RULE_DELETE_INPUTS" -> {
                stateAndRefs
                    .filter { it.state.contractState !is FungibleState<*> }
                    .map { it.ref } to emptyList()
            }
            "CONTRACT_RULE_DELETE_POSITIVE_QUANTITIES" -> {
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
            "CONTRACT_RULE_DELETE_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger.ONE,
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger.TEN,
                        owner = key
                    )
                )
            }
            "CONTRACT_RULE_DELETE_GROUP_SUM" -> {
                stateAndRefs.map { it.ref } to listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger(BigInteger.valueOf(2)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(3)),
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger(BigInteger.valueOf(4)),
                        owner = key
                    ),
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
