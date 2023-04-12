package net.cordapp.demo.utxo.chainable.workflow

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder
import net.cordapp.demo.utxo.chainable.contract.Vehicle
import net.cordapp.demo.utxo.chainable.contract.VehicleContract
import java.time.Instant
import java.time.temporal.ChronoUnit

@Suspendable
internal fun UtxoTransactionBuilder.addIssuedVehicle(vehicle: Vehicle, notary: MemberX500Name) = this
    .addOutputState(vehicle)
    .addCommand(VehicleContract.Issue())
    .addSignatories(vehicle.manufacturer)
    .setNotary(notary)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plus(60, ChronoUnit.MINUTES))

@Suspendable
internal fun UtxoTransactionBuilder.addTransferredVehicle(oldVehicle: StateAndRef<Vehicle>, newVehicle: Vehicle) = this
    .addInputState(oldVehicle.ref)
    .addOutputState(newVehicle)
    .addCommand(VehicleContract.Transfer())
    .addSignatories(oldVehicle.state.contractState.owner)
    .setNotary(oldVehicle.state.notaryName)
    .setTimeWindowBetween(Instant.now().minusSeconds(60), Instant.now().plus(60, ChronoUnit.MINUTES))
