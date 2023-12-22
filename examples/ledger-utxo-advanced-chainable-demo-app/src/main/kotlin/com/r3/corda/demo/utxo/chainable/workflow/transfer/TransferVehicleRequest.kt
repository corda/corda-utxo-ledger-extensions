package com.r3.corda.demo.utxo.chainable.workflow.transfer

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import com.r3.corda.demo.utxo.chainable.contract.Vehicle
import com.r3.corda.demo.utxo.chainable.contract.transferOwnership
import com.r3.corda.demo.utxo.chainable.workflow.firstLedgerKey
import com.r3.corda.demo.utxo.chainable.workflow.getMemberInfo
import java.time.Instant
import java.util.UUID

data class TransferVehicleRequest(
    val id: UUID,
    val owner: String,
    val observers: Collection<String>
) {

    @Suspendable
    fun getInputState(utxoLedgerService: UtxoLedgerService): StateAndRef<Vehicle> {
        return utxoLedgerService
            .findUnconsumedStatesByExactType(Vehicle::class.java, 1, Instant.now())
            .results
            .single { it.state.contractState.id == id }
    }

    @Suspendable
    fun getOutputState(inputState: StateAndRef<Vehicle>, memberLookup: MemberLookup): Vehicle {
        val owner = memberLookup.getMemberInfo(owner).firstLedgerKey
        return inputState.transferOwnership(owner)
    }

    @Suspendable
    fun getFlowSessions(flowMessaging: FlowMessaging): List<FlowSession> {
        val counterpartyNames = observers.map(MemberX500Name::parse)
        return counterpartyNames.map(flowMessaging::initiateFlow)
    }
}
