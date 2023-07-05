package com.r3.corda.demo.utxo.ownable.workflow.testing

import com.r3.corda.demo.utxo.ownable.workflow.firstLedgerKey
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name

class OwnableContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        val ownerName = MemberX500Name.parse(request.owner)
        val owner = requireNotNull(memberLookup.lookup(ownerName)) {
            "owner $ownerName does not exist in the network"
        }.firstLedgerKey
        when (request.command) {
            "UPDATE" -> {
                val outputs = flowEngine.subFlow(OwnableContractCreateTestFlow("VALID", owner, ownerName))
                flowEngine.subFlow(OwnableContractUpdateTestFlow(request.rule, owner, ownerName, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

private class Request(val command: String, val rule: String, val owner: String)
