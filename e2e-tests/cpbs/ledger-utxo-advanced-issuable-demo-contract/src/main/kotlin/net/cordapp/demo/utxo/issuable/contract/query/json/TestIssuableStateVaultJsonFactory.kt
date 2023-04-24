package net.cordapp.demo.utxo.issuable.contract.query.json

import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory
import net.cordapp.demo.utxo.issuable.contract.TestIssuableState

class TestIssuableStateVaultJsonFactory : ContractStateVaultJsonFactory<TestIssuableState> {

    override fun getStateType(): Class<TestIssuableState> {
        return TestIssuableState::class.java
    }

    override fun create(state: TestIssuableState, jsonMarshallingService: JsonMarshallingService): String {
        return "{}"
    }
}
