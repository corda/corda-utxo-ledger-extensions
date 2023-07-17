package com.r3.corda.test.utxo.ownable.contract.query.json

import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory
import com.r3.corda.test.utxo.ownable.contract.TestOwnableState

class TestOwnableStateVaultJsonFactory : ContractStateVaultJsonFactory<TestOwnableState> {

    override fun getStateType(): Class<TestOwnableState> {
        return TestOwnableState::class.java
    }

    override fun create(state: TestOwnableState, jsonMarshallingService: JsonMarshallingService): String {
        return "{}"
    }
}
