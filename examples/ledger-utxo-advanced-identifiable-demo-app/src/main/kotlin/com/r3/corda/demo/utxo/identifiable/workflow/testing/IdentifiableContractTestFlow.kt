package com.r3.corda.demo.utxo.identifiable.workflow.testing

import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.base.annotations.Suspendable

class IdentifiableContractTestFlow : ClientStartableFlow {

    @CordaInject
    private lateinit var flowEngine: FlowEngine

    @CordaInject
    private lateinit var jsonMarshallingService: JsonMarshallingService

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        val request = requestBody.getRequestBodyAs(jsonMarshallingService, Request::class.java)
        when (request.command) {
            "CREATE" -> flowEngine.subFlow(IdentifiableContractCreateTestFlow(request.rule))
            "UPDATE" -> {
                val outputs = flowEngine.subFlow(IdentifiableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(IdentifiableContractUpdateTestFlow(request.rule, outputs))
            }
            "DELETE" -> {
                val outputs = flowEngine.subFlow(IdentifiableContractCreateTestFlow("VALID"))
                flowEngine.subFlow(IdentifiableContractDeleteTestFlow(request.rule, outputs))
            }
            else -> throw IllegalArgumentException("Invalid command type passed in")
        }
        return "success"
    }
}

private class Request(val command: String, val rule: String)
