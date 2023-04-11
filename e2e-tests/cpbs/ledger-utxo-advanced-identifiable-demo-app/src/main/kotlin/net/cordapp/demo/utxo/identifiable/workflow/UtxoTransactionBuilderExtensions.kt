package net.cordapp.demo.utxo.identifiable.workflow

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder
import net.cordapp.demo.utxo.identifiable.contract.SupportTicket
import net.cordapp.demo.utxo.identifiable.contract.SupportTicketContract
import java.time.Instant

@Suspendable
internal fun UtxoTransactionBuilder.addCreatedSupportTicket(ticket: SupportTicket, notary: MemberX500Name) = this
    .addOutputState(ticket)
    .addCommand(SupportTicketContract.Create())
    .addSignatories(ticket.reporter)
    .setNotary(notary)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))

@Suspendable
internal fun UtxoTransactionBuilder.addUpdatedSupportTicket(
    oldTicket: StateAndRef<SupportTicket>,
    newTicket: SupportTicket
) = this
    .addInputState(oldTicket.ref)
    .addOutputState(newTicket)
    .addCommand(SupportTicketContract.Update())
    .addSignatories(newTicket.assignee)
    .setNotary(oldTicket.state.notaryName)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))

@Suspendable
internal fun UtxoTransactionBuilder.addDeletedSupportTicket(ticket: StateAndRef<SupportTicket>) = this
    .addInputState(ticket.ref)
    .addCommand(SupportTicketContract.Delete())
    .addSignatories(ticket.state.contractState.reporter)
    .setNotary(ticket.state.notaryName)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))
