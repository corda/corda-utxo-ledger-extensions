package com.r3.corda.demo.utxo.identifiable.contract.query.json

import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory
import com.r3.corda.demo.utxo.identifiable.contract.SupportTicket

class SupportTicketVaultJsonFactory : ContractStateVaultJsonFactory<SupportTicket> {

    override fun getStateType(): Class<SupportTicket> {
        return SupportTicket::class.java
    }

    override fun create(state: SupportTicket, jsonMarshallingService: JsonMarshallingService): String {
        return "{}"
    }
}
