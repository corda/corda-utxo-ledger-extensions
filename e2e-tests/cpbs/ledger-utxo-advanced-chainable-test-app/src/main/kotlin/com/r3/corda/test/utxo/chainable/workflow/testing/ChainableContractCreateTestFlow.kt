package com.r3.corda.test.utxo.chainable.workflow.testing

import com.r3.corda.test.utxo.chainable.contract.testing.MyChainableContract
import com.r3.corda.test.utxo.chainable.contract.testing.MyChainableState
import com.r3.corda.test.utxo.chainable.contract.testing.MyContractState
import com.r3.corda.test.utxo.chainable.workflow.firstLedgerKey
import com.r3.corda.ledger.utxo.base.StaticPointer
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.crypto.DigestAlgorithmName
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

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
                    MyChainableState(UUID.randomUUID(), key, null),
                    MyChainableState(UUID.randomUUID(), key, null),
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
