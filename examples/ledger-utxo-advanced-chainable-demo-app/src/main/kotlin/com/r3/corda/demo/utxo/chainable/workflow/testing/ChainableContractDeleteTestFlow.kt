package com.r3.corda.demo.utxo.chainable.workflow.testing

import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableContract
import com.r3.corda.demo.utxo.chainable.contract.testing.MyChainableState
import com.r3.corda.demo.utxo.chainable.workflow.firstLedgerKey
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.time.Instant
import java.time.temporal.ChronoUnit

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
