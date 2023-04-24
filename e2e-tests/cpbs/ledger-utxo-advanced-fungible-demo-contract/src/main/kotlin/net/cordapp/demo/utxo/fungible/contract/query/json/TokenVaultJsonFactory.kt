package net.cordapp.demo.utxo.fungible.contract.query.json

import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory
import net.cordapp.demo.utxo.fungible.contract.Token

class TokenVaultJsonFactory : ContractStateVaultJsonFactory<Token> {

    override fun getStateType(): Class<Token> {
        return Token::class.java
    }

    override fun create(state: Token, jsonMarshallingService: JsonMarshallingService): String {
        return "{}"
    }
}
