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
class ChainableTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-chainable-demo-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-chainable-demo-app.cpb"

        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
        }
    }

    private val testRunUniqueId = UUID.randomUUID()
    private val cpiName = "${TEST_CPI_NAME}_$testRunUniqueId"
    private val notaryCpiName = "${TEST_NOTARY_CPI_NAME}_$testRunUniqueId"

    private val aliceX500 = "CN=Alice-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val bobX500 = "CN=Bob-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val charlieX500 = "CN=Charlie-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val notaryX500 = "CN=Notary-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"

    private val aliceHoldingId: String = getHoldingIdShortHash(aliceX500, GROUP_ID)
    private val bobHoldingId: String = getHoldingIdShortHash(bobX500, GROUP_ID)
    private val charlieHoldingId: String = getHoldingIdShortHash(charlieX500, GROUP_ID)
    private val notaryHoldingId: String = getHoldingIdShortHash(notaryX500, GROUP_ID)

    private val staticMemberList = listOf(
        aliceX500,
        bobX500,
        charlieX500,
        notaryX500
    )

    @BeforeAll
    fun beforeAll() {
        uploadTrustedCertificate()
        conditionallyUploadCordaPackage(cpiName, TEST_CPB_LOCATION, GROUP_ID, staticMemberList)
        conditionallyUploadCordaPackage(notaryCpiName, TEST_NOTARY_CPB_LOCATION, GROUP_ID, staticMemberList)

        val aliceActualHoldingId = getOrCreateVirtualNodeFor(aliceX500, cpiName)
        val bobActualHoldingId = getOrCreateVirtualNodeFor(bobX500, cpiName)
        val charlieActualHoldingId = getOrCreateVirtualNodeFor(charlieX500, cpiName)
        val notaryActualHoldingId = getOrCreateVirtualNodeFor(notaryX500, notaryCpiName)

        assertThat(aliceActualHoldingId).isEqualTo(aliceHoldingId)
        assertThat(bobActualHoldingId).isEqualTo(bobHoldingId)
        assertThat(charlieActualHoldingId).isEqualTo(charlieHoldingId)
        assertThat(notaryActualHoldingId).isEqualTo(notaryHoldingId)

        registerStaticMember(aliceHoldingId)
        registerStaticMember(bobHoldingId)
        registerStaticMember(charlieHoldingId)
        registerStaticMember(notaryHoldingId, true)
    }

    @Test
    fun `Alice issues vehicle to Bob, bob transfers vehicle to Charlie`() {

        // Alice issues vehicle to Bob
        val vehicleId = UUID.randomUUID()

        val issueVehicleFlowRequestId = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "make" to "reliant",
                "model" to "robin",
                "id" to vehicleId,
                "manufacturer" to aliceX500,
                "owner" to bobX500,
                "notary" to "O=MyNotaryService, L=London, C=GB",
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.chainable.workflow.issue.IssueVehicleFlow\$Initiator"
        )
        val issueVehicleFlowResult = awaitRpcFlowFinished(aliceHoldingId, issueVehicleFlowRequestId)
        assertThat(issueVehicleFlowResult.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(issueVehicleFlowResult.flowError).isNull()

        val issuedVehicleResponse = objectMapper
            .readValue(issueVehicleFlowResult.flowResult, VehicleResponse::class.java)

        assertThat(issuedVehicleResponse.make).isEqualTo("reliant")
        assertThat(issuedVehicleResponse.model).isEqualTo("robin")
        assertThat(issuedVehicleResponse.id).isEqualTo(vehicleId)
        assertThat(issuedVehicleResponse.manufacturer).isEqualTo(aliceX500)
        assertThat(issuedVehicleResponse.owner).isEqualTo(bobX500)

        // Bob transfers vehicle to Charlie
        val transferVehicleRequestId = startRpcFlow(
            bobHoldingId,
            mapOf(
                "id" to vehicleId,
                "owner" to charlieX500,
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.chainable.workflow.transfer.TransferVehicleFlow\$Initiator"
        )
        val transferVehicleFlowResult = awaitRpcFlowFinished(bobHoldingId, transferVehicleRequestId)
        assertThat(transferVehicleFlowResult.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(transferVehicleFlowResult.flowError).isNull()

        val transferVehicleResponse = objectMapper
            .readValue(transferVehicleFlowResult.flowResult, VehicleResponse::class.java)

        assertThat(transferVehicleResponse.make).isEqualTo("reliant")
        assertThat(transferVehicleResponse.model).isEqualTo("robin")
        assertThat(transferVehicleResponse.id).isEqualTo(vehicleId)
        assertThat(transferVehicleResponse.manufacturer).isEqualTo(aliceX500)
        assertThat(transferVehicleResponse.owner).isEqualTo(charlieX500)
    }

    data class VehicleResponse(
        val make: String,
        val model: String,
        val id: UUID,
        val manufacturer: String,
        val owner: String
    )
}
