package com.r3.corda.demo.utxo.identifiable.contract

import net.corda.v5.base.annotations.CordaSerializable

@CordaSerializable
enum class SupportTicketStatus {
    TODO,
    OPEN,
    DONE;

    fun canTransitionTo(newStatus: SupportTicketStatus): Boolean = when(this) {
        TODO -> newStatus == OPEN
        OPEN -> newStatus == DONE
        DONE -> false
    }
}
