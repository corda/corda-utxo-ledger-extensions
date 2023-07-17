package com.r3.corda.test.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.NumericInteger
import com.r3.corda.test.utxo.fungible.contract.MyContractState
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
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class FungibleContractCreateTestFlow(private val rule: String) : SubFlow<List<StateAndRef<*>>> {

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
            "CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES" -> {
                listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger.ZERO,
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger.TEN,
                        owner = key
                    ),
                )
            }
            "VALID" -> {
                listOf(
                    MyFungibleStateA(
                        quantity = NumericInteger.ONE,
                        owner = key
                    ),
                    MyFungibleStateB(
                        quantity = NumericInteger.TEN,
                        owner = key
                    ),
                    MyContractState(UUID.randomUUID())
                )
            }
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addOutputStates(outputs)
            .addSignatories(key)
            .addCommand(MyFungibleContract.Create())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        return utxoLedgerService.finalize(transaction, emptyList()).transaction.outputStateAndRefs
    }
}
