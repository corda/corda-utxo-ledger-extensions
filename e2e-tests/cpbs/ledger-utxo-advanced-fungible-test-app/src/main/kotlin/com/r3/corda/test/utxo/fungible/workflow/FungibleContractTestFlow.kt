package com.r3.corda.test.utxo.fungible.workflow

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.base.annotations.Suspendable

class FungibleContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        when (request.command) {
            "CREATE" -> flowEngine.subFlow(FungibleContractCreateTestFlow(request.rule))
            "UPDATE" -> {
                val outputs = flowEngine.subFlow(FungibleContractCreateTestFlow("VALID"))
                flowEngine.subFlow(FungibleContractUpdateTestFlow(request.rule, outputs))
            }
            "DELETE" -> {
                val outputs = flowEngine.subFlow(FungibleContractCreateTestFlow("VALID"))
                flowEngine.subFlow(FungibleContractDeleteTestFlow(request.rule, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

private class Request(val command: String, val rule: String)
