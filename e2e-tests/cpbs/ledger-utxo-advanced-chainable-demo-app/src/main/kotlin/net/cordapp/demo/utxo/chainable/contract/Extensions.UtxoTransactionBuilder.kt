package net.cordapp.demo.utxo.chainable.contract

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder
import java.time.Instant

@Suspendable
internal fun UtxoTransactionBuilder.addIssuedVehicle(vehicle: Vehicle, notary: MemberX500Name) = this
    .addOutputState(vehicle)
    .addCommand(VehicleContract.Issue())
    .addSignatories(vehicle.manufacturer)
    .setNotary(notary)
    .setTimeWindowBetween(Instant.MIN, Instant.MAX)

@Suspendable
internal fun UtxoTransactionBuilder.addTransferredVehicle(oldVehicle: StateAndRef<Vehicle>, newVehicle: Vehicle) = this
    .addInputState(oldVehicle.ref)
    .addOutputState(newVehicle)
    .addCommand(VehicleContract.Transfer())
    .addSignatories(oldVehicle.state.contractState.owner)
    .setNotary(oldVehicle.state.notaryName)
    .setTimeWindowBetween(Instant.MIN, Instant.MAX)
