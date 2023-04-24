package net.cordapp.demo.utxo.chainable.contract.query.json

import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory
import net.cordapp.demo.utxo.chainable.contract.Vehicle

class VehicleVaultJsonFactory : ContractStateVaultJsonFactory<Vehicle> {

    override fun getStateType(): Class<Vehicle> {
        return Vehicle::class.java
    }

    override fun create(state: Vehicle, jsonMarshallingService: JsonMarshallingService): String {
        return "{}"
    }
}
