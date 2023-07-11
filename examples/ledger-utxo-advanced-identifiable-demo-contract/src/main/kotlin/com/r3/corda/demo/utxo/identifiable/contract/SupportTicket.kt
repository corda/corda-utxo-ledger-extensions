package com.r3.corda.demo.utxo.identifiable.contract

import com.r3.corda.ledger.utxo.base.VisibleState
import com.r3.corda.ledger.utxo.identifiable.IdentifiableState
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.VisibilityChecker
import java.security.PublicKey

@BelongsToContract(SupportTicketContract::class)
data class SupportTicket(
    val title: String,
    val description: String,
    val reporter: PublicKey,
    val assignee: PublicKey,
    val status: SupportTicketStatus = SupportTicketStatus.TODO,
    private val id: StateRef? = null
) : IdentifiableState, VisibleState {

    override fun getParticipants(): List<PublicKey> {
        return listOf(reporter, assignee)
    }

    override fun getId(): StateRef? {
        return id
    }

    override fun isVisible(checker: VisibilityChecker): Boolean {
        return true
    }
}
