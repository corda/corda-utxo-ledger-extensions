package com.r3.corda.test.utxo.issuable.workflow

import com.r3.corda.test.utxo.issuable.contract.MyIssuableContract
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

@InitiatingFlow(protocol = "IssuableContractDeleteTestFlow")
class IssuableContractDeleteTestFlow(
    private val rule: String,
    private val issuer: PublicKey,
    private val issuerName: MemberX500Name,
    private val stateAndRefs: List<StateAndRef<*>>
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
            "CONTRACT_RULE_DELETE_SIGNATORIES" -> listOf(key)
            "VALID" -> listOf(issuer, key)
            else -> throw IllegalArgumentException("Invalid rule type passed in")
        }
        val transaction = utxoLedgerService.createTransactionBuilder()
            .setNotary(notaryLookup.notaryServices.first().name)
            .addInputStates(stateAndRefs.map { it.ref })
            .addSignatories(signatories)
            .addCommand(MyIssuableContract.Delete())
            .setTimeWindowUntil(Instant.now().plus(10, ChronoUnit.DAYS))
            .toSignedTransaction()

        val session = flowMessaging.initiateFlow(issuerName)
        return utxoLedgerService.finalize(transaction, listOf(session)).transaction.outputStateAndRefs
    }
}

@InitiatedBy(protocol = "IssuableContractDeleteTestFlow")
class IssuableContractDeleteTestResponderFlow : ResponderFlow {

    @CordaInject
    private lateinit var utxoLedgerService: UtxoLedgerService

    @Suspendable
    override fun call(session: FlowSession) {
        utxoLedgerService.receiveFinality(session) {
            // accept
        }
    }
}
