package com.r3.corda.test.utxo.issuable.workflow

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name

class IssuableContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @CordaInject
    private lateinit var memberLookup: MemberLookup

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        val issuerName = MemberX500Name.parse(request.issuer)
        val issuer = requireNotNull(memberLookup.lookup(issuerName)) {
            "Issuer $issuerName does not exist in the network"
        }.firstLedgerKey
        when (request.command) {
            "CREATE" -> flowEngine.subFlow(IssuableContractCreateTestFlow(request.rule, issuer, issuerName))
            "DELETE" -> {
                val outputs = flowEngine.subFlow(IssuableContractCreateTestFlow("VALID", issuer, issuerName))
                flowEngine.subFlow(IssuableContractDeleteTestFlow(request.rule, issuer, issuerName, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

private class Request(val command: String, val rule: String, val issuer: String)
