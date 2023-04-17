package com.r3.corda.ledger.utxo.e2etest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.corda.e2etest.utilities.GROUP_ID
import net.corda.e2etest.utilities.RPC_FLOW_STATUS_SUCCESS
import net.corda.e2etest.utilities.TEST_NOTARY_CPB_LOCATION
import net.corda.e2etest.utilities.TEST_NOTARY_CPI_NAME
import net.corda.e2etest.utilities.awaitRpcFlowFinished
import net.corda.e2etest.utilities.conditionallyUploadCordaPackage
import net.corda.e2etest.utilities.getHoldingIdShortHash
import net.corda.e2etest.utilities.getOrCreateVirtualNodeFor
import net.corda.e2etest.utilities.registerStaticMember
import net.corda.e2etest.utilities.startRpcFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.UUID

@Suppress("Unused", "FunctionName")
@TestInstance(PER_CLASS)
class OwnableTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-ownable-demo-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-ownable-demo-app.cpb"

        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
    }

    private val testRunUniqueId = UUID.randomUUID()
    private val cpiName = "${TEST_CPI_NAME}_$testRunUniqueId"
    private val notaryCpiName = "${TEST_NOTARY_CPI_NAME}_$testRunUniqueId"

    private val aliceX500 = "CN=Alice-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val bobX500 = "CN=Bob-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val notaryX500 = "CN=Notary-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"

    private val aliceHoldingId: String = getHoldingIdShortHash(aliceX500, GROUP_ID)
    private val bobHoldingId: String = getHoldingIdShortHash(bobX500, GROUP_ID)
    private val notaryHoldingId: String = getHoldingIdShortHash(notaryX500, GROUP_ID)

    private val staticMemberList = listOf(
        aliceX500,
        bobX500,
        notaryX500
    )

    @BeforeAll
    fun beforeAll() {
        uploadTrustedCertificate()
        conditionallyUploadCordaPackage(cpiName, TEST_CPB_LOCATION, GROUP_ID, staticMemberList)
        conditionallyUploadCordaPackage(notaryCpiName, TEST_NOTARY_CPB_LOCATION, GROUP_ID, staticMemberList)

        val aliceActualHoldingId = getOrCreateVirtualNodeFor(aliceX500, cpiName)
        val bobActualHoldingId = getOrCreateVirtualNodeFor(bobX500, cpiName)
        val notaryActualHoldingId = getOrCreateVirtualNodeFor(notaryX500, notaryCpiName)

        assertThat(aliceActualHoldingId).isEqualTo(aliceHoldingId)
        assertThat(bobActualHoldingId).isEqualTo(bobHoldingId)
        assertThat(notaryActualHoldingId).isEqualTo(notaryHoldingId)

        registerStaticMember(aliceHoldingId)
        registerStaticMember(bobHoldingId)
        registerStaticMember(notaryHoldingId, true)
    }

    @Test
    fun `query ownable states`() {

        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(),
            "net.cordapp.demo.utxo.ownable.workflow.query.OwnableStateQueryFlow"
        )
        val createFlowResponse = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, OwnableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()
        assertThat(response.after).hasSize(2)
        assertThat(response.consumed).isEmpty()
    }

    @Test
    fun `query well known ownable states`() {

        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(),
            "net.cordapp.demo.utxo.ownable.workflow.query.WellKnownOwnableStateQueryFlow"
        )
        val createFlowResponse = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, WellKnownOwnableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()
        assertThat(response.after).hasSize(2)
        assertThat(response.consumed).isEmpty()
    }

    data class OwnableStateQueryResponse(
        val before: List<OwnableStateValues>,
        val after: List<OwnableStateValues>,
        val consumed: List<OwnableStateValues>
    )

    data class OwnableStateValues(val owner: String)

    data class WellKnownOwnableStateQueryResponse(
        val before: List<WellKnownOwnableStateValues>,
        val after: List<WellKnownOwnableStateValues>,
        val consumed: List<WellKnownOwnableStateValues>
    )

    data class WellKnownOwnableStateValues(val ownerName: String)
}