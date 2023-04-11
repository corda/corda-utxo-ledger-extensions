package net.cordapp.demo.utxo.chainable.contract

import com.r3.corda.ledger.utxo.base.StaticPointer
import com.r3.corda.ledger.utxo.chainable.ChainableState
import com.r3.corda.ledger.utxo.ownable.OwnableState
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.StateRef
import java.security.PublicKey
import java.util.UUID

@BelongsToContract(VehicleContract::class)
data class Vehicle(
    val make: String,
    val model: String,
    val id: UUID,
    val manufacturer: PublicKey,
    private val owner: PublicKey,
    private val previousStatePointer: StaticPointer<Vehicle>?
) : ChainableState<Vehicle>, OwnableState {

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(owner)
    }

    override fun getPreviousStatePointer(): StaticPointer<Vehicle>? {
        return previousStatePointer
    }

    fun next(ref: StateRef): Vehicle {
        return copy(previousStatePointer = StaticPointer(ref, Vehicle::class.java))
    }
}