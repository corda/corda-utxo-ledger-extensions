package com.r3.corda.test.utxo.chainable.workflow

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.base.annotations.Suspendable

class ChainableContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        when (request.command) {
            "CREATE" -> flowEngine.subFlow(ChainableContractCreateTestFlow(request.rule))
            "UPDATE" -> {
                val outputs = flowEngine.subFlow(ChainableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(ChainableContractUpdateTestFlow(request.rule, outputs))
            }
            "DELETE" -> {
                val outputs = flowEngine.subFlow(ChainableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(ChainableContractDeleteTestFlow(request.rule, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

private class Request(val command: String, val rule: String)
