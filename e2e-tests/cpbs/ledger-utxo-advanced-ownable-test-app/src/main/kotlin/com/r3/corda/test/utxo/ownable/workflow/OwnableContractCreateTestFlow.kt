package com.r3.corda.test.utxo.ownable.workflow

import com.r3.corda.test.utxo.ownable.contract.MyContractState
import com.r3.corda.test.utxo.ownable.contract.MyOwnableContract
import com.r3.corda.test.utxo.ownable.contract.MyOwnableState
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.InitiatedBy
import net.corda.v5.application.flows.InitiatingFlow
import net.corda.v5.application.flows.ResponderFlow
import net.corda.v5.application.flows.SubFlow
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import java.security.PublicKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@InitiatingFlow(protocol = "OwnableContractCreateTestFlow")
class OwnableContractCreateTestFlow(
    private val rule: String,
    private val owner: PublicKey,
    private val ownerName: MemberX500Name
) : SubFlow<List<StateAndRef<*>>> {

    @CordaInject
    private lateinit var flowMessaging: FlowMessaging

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @CordaInject
    private lateinit var notaryLookup: NotaryLookup

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(): List<StateAndRef<*>> {
        val key = memberLookup.myInfo().firstLedgerKey
        val signatories = when (rule) {
            "VALID" -> listOf(owner, key)
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val outputs = listOf(
            MyOwnableState(owner, ownerName, participants = listOf(key)),
            MyOwnableState(owner, ownerName, participants = listOf(key)),
            MyContractState(UUID.randomUUID())
        )
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addOutputStates(outputs)
            .addSignatories(signatories)
            .addCommand(MyOwnableContract.Create())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        val session = flowMessaging.initiateFlow(ownerName)
        return utxoLedgerService.finalize(transaction, listOf(session)).transaction.outputStateAndRefs
    }
}

@InitiatedBy(protocol = "OwnableContractCreateTestFlow")
class OwnableContractCreateTestResponderFlow : ResponderFlow {

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(session: FlowSession) {
        utxoLedgerService.receiveFinality(session) {
            // accept
        }
    }
}
