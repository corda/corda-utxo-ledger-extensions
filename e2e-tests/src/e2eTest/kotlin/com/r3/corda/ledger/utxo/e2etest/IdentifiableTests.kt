package com.r3.corda.ledger.utxo.e2etest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.corda.e2etest.utilities.RPC_FLOW_STATUS_FAILED
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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.UUID

@Suppress("Unused", "FunctionName")
@TestInstance(PER_CLASS)
class IdentifiableTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-identifiable-test-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-identifiable-test-app.cpb"
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
    fun `query identifiable states`() {

        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableStateQueryFlow"
        )
        val createFlowResponse = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, IdentifiableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()

        assertThat(response.after).hasSize(1)
        assertThat(response.after.single().id).isNull()

        assertThat(response.updated).hasSize(1)
        assertThat(response.updated.single().id).isEqualTo(response.after.single().stateRef)
        assertThat(response.updated.single().stateRef).isNotEqualTo(response.updated.single().id)

        assertThat(response.consumed).isEmpty()
    }

    @Test
    fun `identifiable pointer resolution`() {

        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiablePointerFlow"
        )
        val createFlowResponse = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(createFlowResponse.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val response = objectMapper
            .readValue(createFlowResponse.flowResult, IdentifiableStateQueryResponse::class.java)

        assertThat(response.before).isEmpty()

        assertThat(response.after).hasSize(1)
        assertThat(response.after.single().id).isNull()

        assertThat(response.updated).hasSize(1)
        assertThat(response.updated.single().id).isEqualTo(response.after.single().stateRef)
        assertThat(response.updated.single().stateRef).isNotEqualTo(response.updated.single().id)

        assertThat(response.consumed).isEmpty()
    }

    @Test
    fun `Identifiable contract create command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "VALID"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `Identifiable contract create command CONTRACT_RULE_CREATE_OUTPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "CONTRACT_RULE_CREATE_OUTPUTS"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On identifiable state(s) creating, at least one identifiable state must be created.")
    }

    @Test
    fun `Identifiable contract update command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "VALID"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `Identifiable contract update command CONTRACT_RULE_UPDATE_INPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_INPUTS"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On identifiable state(s) updating, at least one identifiable state must be consumed.")
    }

    @Test
    fun `Identifiable contract update command CONTRACT_RULE_UPDATE_OUTPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_OUTPUTS"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On identifiable state(s) updating, at least one identifiable state must be created.")
    }

    @Test
    fun `Identifiable contract update command CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On identifiable state(s) updating, each created identifiable state's identifier must match one consumed identifiable " +
                        "state's state ref or identifier, exclusively."
            )
    }

    // TODO Currently broken fixed in CORE-13473
    @Test
    @Disabled
    fun `Identifiable contract update command CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY MISSING_ID fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY MISSING_ID"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On identifiable state(s) updating, each created identifiable state's identifier must match one consumed identifiable " +
                        "state's state ref or identifier, exclusively."
            )
    }

    @Test
    fun `Identifiable contract delete command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "VALID"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `Identifiable contract delete command CONTRACT_RULE_DELETE_INPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_INPUTS"
            ),
            "com.r3.corda.test.utxo.identifiable.workflow.IdentifiableContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On identifiable state(s) deleting, at least one identifiable state must be consumed.")
    }

    data class IdentifiableStateQueryResponse(
        val before: List<IdentifiableStateValues>,
        val after: List<IdentifiableStateValues>,
        val updated: List<IdentifiableStateValues>,
        val consumed: List<IdentifiableStateValues>
    )
    data class IdentifiableStateValues(val stateRef: String, val id: String?)
}
