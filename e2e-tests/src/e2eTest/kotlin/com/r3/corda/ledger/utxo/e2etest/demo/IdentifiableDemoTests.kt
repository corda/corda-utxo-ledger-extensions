package com.r3.corda.ledger.utxo.e2etest.demo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.r3.corda.ledger.utxo.e2etest.uploadTrustedCertificate
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
class IdentifiableDemoTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-identifiable-demo-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-identifiable-demo-app.cpb"
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
    fun `Alice issues a support ticket to Bob, Bob opens and completes the ticket, Alice closes the ticket`() {

        val issueSupportTicketRequestId = startRestFlow(
            aliceHoldingId,
            mapOf(
                "title" to "Build Corda 5",
                "description" to "Build super-duper DLT and call it Corda 5",
                "reporter" to aliceX500,
                "assignee" to bobX500,
                "notary" to NOTARY_SERVICE_X500,
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.identifiable.workflow.create.CreateSupportTicketFlow\$Initiator"
        )
        val createFlowResponse = awaitRestFlowFinished(aliceHoldingId, issueSupportTicketRequestId)
        assertThat(createFlowResponse.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(createFlowResponse.flowError).isNull()

        val createSupportTicketResponse = objectMapper
            .readValue(createFlowResponse.flowResult, CreateSupportTicketResponse::class.java)

        val openSupportTicketRequestId = startRestFlow(
            bobHoldingId,
            mapOf(
                "id" to createSupportTicketResponse.id,
                "reporter" to aliceX500,
                "status" to "OPEN",
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.identifiable.workflow.update.UpdateSupportTicketFlow\$Initiator"
        )
        val openFlowResult = awaitRestFlowFinished(bobHoldingId, openSupportTicketRequestId)
        assertThat(openFlowResult.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(openFlowResult.flowError).isNull()

        val openSupportTicketResponse = objectMapper
            .readValue(openFlowResult.flowResult, UpdateSupportTicketResponse::class.java)

        assertThat(openSupportTicketResponse.id).isEqualTo(createSupportTicketResponse.id)
        assertThat(openSupportTicketResponse.title).isEqualTo(createSupportTicketResponse.title)

        val doneSupportTicketRequestId = startRestFlow(
            bobHoldingId,
            mapOf(
                "id" to openSupportTicketResponse.id,
                "reporter" to aliceX500,
                "status" to "DONE",
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.identifiable.workflow.update.UpdateSupportTicketFlow\$Initiator"
        )
        val doneFlowResult = awaitRestFlowFinished(bobHoldingId, doneSupportTicketRequestId)
        assertThat(doneFlowResult.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(doneFlowResult.flowError).isNull()

        val doneSupportTicketResponse = objectMapper
            .readValue(doneFlowResult.flowResult, UpdateSupportTicketResponse::class.java)

        assertThat(doneSupportTicketResponse.id).isEqualTo(openSupportTicketResponse.id)
        assertThat(doneSupportTicketResponse.title).isEqualTo(openSupportTicketResponse.title)

        val deleteSupportTicketRequestId = startRestFlow(
            aliceHoldingId,
            mapOf(
                "id" to doneSupportTicketResponse.id,
                "assignee" to bobX500,
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.identifiable.workflow.delete.DeleteSupportTicketFlow\$Initiator"
        )

        val deleteSupportTicketResult = awaitRestFlowFinished(aliceHoldingId, deleteSupportTicketRequestId)
        assertThat(deleteSupportTicketResult.flowStatus).isEqualTo(REST_FLOW_STATUS_SUCCESS)
        assertThat(deleteSupportTicketResult.flowError).isNull()

        val deleteSupportTicketResponse = objectMapper
            .readValue(doneFlowResult.flowResult, DeleteSupportTicketResponse::class.java)

        assertThat(deleteSupportTicketResponse.id).isEqualTo(doneSupportTicketResponse.id)
        assertThat(deleteSupportTicketResponse.title).isEqualTo(doneSupportTicketResponse.title)
    }

    data class CreateSupportTicketResponse(val id: String, val title: String)
    data class UpdateSupportTicketResponse(val id: String, val title: String)
    data class DeleteSupportTicketResponse(val id: String, val title: String)
}
