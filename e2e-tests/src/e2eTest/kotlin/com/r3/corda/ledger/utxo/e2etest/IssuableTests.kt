package com.r3.corda.ledger.utxo.e2etest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.corda.e2etest.utilities.REST_FLOW_STATUS_FAILED
import net.corda.e2etest.utilities.REST_FLOW_STATUS_SUCCESS
import net.corda.e2etest.utilities.TEST_NOTARY_CPB_LOCATION
import net.corda.e2etest.utilities.TEST_NOTARY_CPI_NAME
import net.corda.e2etest.utilities.awaitRestFlowFinished
import net.corda.e2etest.utilities.conditionallyUploadCordaPackage
import net.corda.e2etest.utilities.getHoldingIdShortHash
import net.corda.e2etest.utilities.getOrCreateVirtualNodeFor
import net.corda.e2etest.utilities.registerStaticMember
import net.corda.e2etest.utilities.startRestFlow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.UUID

@Suppress("Unused", "FunctionName")
@TestInstance(PER_CLASS)
class IssuableTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-issuable-test-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-issuable-test-app.cpb"
        const val NOTARY_SERVICE_X500 = "O=MyNotaryService, L=London, C=GB"

        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
    }

    private val testRunUniqueId = UUID.randomUUID()
    private val groupId = UUID.randomUUID().toString()
    private val cpiName = "${TEST_CPI_NAME}_$testRunUniqueId"
    private val notaryCpiName = "${TEST_NOTARY_CPI_NAME}_$testRunUniqueId"

    private val aliceX500 = "CN=Alice-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val bobX500 = "CN=Bob-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val notaryX500 = "CN=Notary-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"

    private val aliceHoldingId: String = getHoldingIdShortHash(aliceX500, groupId)
    private val bobHoldingId: String = getHoldingIdShortHash(bobX500, groupId)
    private val notaryHoldingId: String = getHoldingIdShortHash(notaryX500, groupId)

    private val staticMemberList = listOf(
        aliceX500,
        bobX500,
        notaryX500
    )

    @BeforeAll
    fun beforeAll() {
        uploadTrustedCertificate()
        conditionallyUploadCordaPackage(cpiName, TEST_CPB_LOCATION, groupId, staticMemberList)
        conditionallyUploadCordaPackage(notaryCpiName, TEST_NOTARY_CPB_LOCATION, groupId, staticMemberList)

        val aliceActualHoldingId = getOrCreateVirtualNodeFor(aliceX500, cpiName)
        val bobActualHoldingId = getOrCreateVirtualNodeFor(bobX500, cpiName)
        val notaryActualHoldingId = getOrCreateVirtualNodeFor(notaryX500, notaryCpiName)

        assertThat(aliceActualHoldingId).isEqualTo(aliceHoldingId)
        assertThat(bobActualHoldingId).isEqualTo(bobHoldingId)
        assertThat(notaryActualHoldingId).isEqualTo(notaryHoldingId)

        registerStaticMember(aliceHoldingId)
        registerStaticMember(bobHoldingId)
        registerStaticMember(notaryHoldingId, NOTARY_SERVICE_X500)
    }

    @Test
    fun `query issuable states`() {

        val request = startRestFlow(
            aliceHoldingId,
            mapOf(),
            "com.r3.corda.test.utxo.issuable.workflow.IssuableStateQueryFlow"
        )
        val createFlowResponse = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, IssuableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()
        assertThat(response.after).hasSize(2)
        assertThat(response.consumed).isEmpty()
    }

    @Test
    fun `query well known issuable states`() {

        val request = startRestFlow(
            aliceHoldingId,
            mapOf(),
            "com.r3.corda.test.utxo.issuable.workflow.WellKnownIssuableStateQueryFlow"
        )
        val createFlowResponse = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, WellKnownIssuableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()
        assertThat(response.after).hasSize(2)
        assertThat(response.consumed).isEmpty()
    }

    @Test
    fun `Issuable contract create command valid`() {
        val request = startRestFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "VALID",
                "issuer" to bobX500
            ),
            "com.r3.corda.test.utxo.issuable.workflow.IssuableContractTestFlow"
        )
        val response = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `Issuable contract create command CONTRACT_RULE_CREATE_SIGNATORIES fails`() {
        val request = startRestFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "CONTRACT_RULE_CREATE_SIGNATORIES",
                "issuer" to bobX500
            ),
            "com.r3.corda.test.utxo.issuable.workflow.IssuableContractTestFlow"
        )
        val response = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(REST_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On issuable state(s) creating, the issuer of every created issuable state must sign the transaction.")
    }

    @Test
    fun `Issuable contract delete command valid`() {
        val request = startRestFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "VALID",
                "issuer" to bobX500
            ),
            "com.r3.corda.test.utxo.issuable.workflow.IssuableContractTestFlow"
        )
        val response = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `Issuable contract delete command CONTRACT_RULE_DELETE_SIGNATORIES fails`() {
        val request = startRestFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_SIGNATORIES",
                "issuer" to bobX500
            ),
            "com.r3.corda.test.utxo.issuable.workflow.IssuableContractTestFlow"
        )
        val response = awaitRestFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(REST_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction.")
    }

    data class IssuableStateQueryResponse(
        val before: List<IssuableStateValues>,
        val after: List<IssuableStateValues>,
        val consumed: List<IssuableStateValues>
    )

    data class IssuableStateValues(val issuer: String)

    data class WellKnownIssuableStateQueryResponse(
        val before: List<WellKnownIssuableStateValues>,
        val after: List<WellKnownIssuableStateValues>,
        val consumed: List<WellKnownIssuableStateValues>
    )

    data class WellKnownIssuableStateValues(val issuerName: String)
}
