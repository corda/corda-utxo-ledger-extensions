package com.r3.corda.ledger.utxo.e2etest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.corda.e2etest.utilities.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.math.BigDecimal
import java.util.*

@Suppress("Unused", "FunctionName")
@TestInstance(PER_CLASS)
class FungibleTests {

    private companion object {
        const val TEST_CPI_NAME = "corda-ledger-extensions-ledger-utxo-advanced-fungible-demo-app"
        const val TEST_CPB_LOCATION = "/META-INF/corda-ledger-extensions-ledger-utxo-advanced-fungible-demo-app.cpb"

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
    private val charlieX500 = "CN=Charlie-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"
    private val notaryX500 = "CN=Notary-${testRunUniqueId}, OU=Application, O=R3, L=London, C=GB"

    private val aliceHoldingId: String = getHoldingIdShortHash(aliceX500, groupId)
    private val bobHoldingId: String = getHoldingIdShortHash(bobX500, groupId)
    private val charlieHoldingId: String = getHoldingIdShortHash(charlieX500, groupId)
    private val notaryHoldingId: String = getHoldingIdShortHash(notaryX500, groupId)

    private val staticMemberList = listOf(
        aliceX500,
        bobX500,
        charlieX500,
        notaryX500
    )

    private fun Double.toScaledBigDecimal(scale: Int = 2): BigDecimal {
        return this.toBigDecimal().setScale(scale)
    }

    @BeforeAll
    fun beforeAll() {
        uploadTrustedCertificate()
        conditionallyUploadCordaPackage(cpiName, TEST_CPB_LOCATION, groupId, staticMemberList)
        conditionallyUploadCordaPackage(notaryCpiName, TEST_NOTARY_CPB_LOCATION, groupId, staticMemberList)

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
    fun `Alice issues a token to Bob, Bob then transfers to Charlie, and then Bob burns some quantity of their token`() {

        // Alice issues tokens to Bob
        val mintTokenFlowRequestId = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "issuer" to aliceX500,
                "owner" to bobX500,
                "quantity" to 123.45.toScaledBigDecimal(),
                "notary" to "O=MyNotaryService-$notaryHoldingId, L=London, C=GB",
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.mint.MintTokenFlow\$Initiator"
        )
        val mintTokenFlowResult = awaitRpcFlowFinished(aliceHoldingId, mintTokenFlowRequestId)
        assertThat(mintTokenFlowResult.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(mintTokenFlowResult.flowError).isNull()

        val mintTokenResponse = objectMapper.readValue(mintTokenFlowResult.flowResult, MintTokenResponse::class.java)

        assertThat(mintTokenResponse.balance.keys).hasSize(1)
        assertThat(mintTokenResponse.balance.values).hasSize(1)

        val owner = mintTokenResponse.balance.keys.single()
        val quantity = mintTokenResponse.balance.values.single()

        assertThat(owner).isEqualTo(bobX500)
        assertThat(quantity).isEqualTo(123.45.toScaledBigDecimal())

        // Bob transfers tokens to Charlie and receives change
        val moveTokenFlowRequestId = startRpcFlow(
            bobHoldingId,
            mapOf(
                "issuer" to aliceX500,
                "owner" to bobX500,
                "shares" to mapOf(
                    charlieX500 to 100.41.toScaledBigDecimal()
                ),
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.move.MoveTokenFlow\$Initiator"
        )
        val moveTokenFlowResult = awaitRpcFlowFinished(bobHoldingId, moveTokenFlowRequestId)
        assertThat(moveTokenFlowResult.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(moveTokenFlowResult.flowError).isNull()

        val moveTokenResponse = objectMapper.readValue(moveTokenFlowResult.flowResult, MoveTokenResponse::class.java)

        assertThat(moveTokenResponse.balance.keys).hasSize(2)
        assertThat(moveTokenResponse.balance.values).hasSize(2)

        assertThat(moveTokenResponse.balance.keys).containsExactlyInAnyOrder(bobX500, charlieX500)
        assertThat(moveTokenResponse.balance.values).containsExactlyInAnyOrder(
            100.41.toScaledBigDecimal(),
            23.04.toScaledBigDecimal()
        )

        // Bob redeems tokens
        val burnTokenFlowRequestId = startRpcFlow(
            bobHoldingId,
            mapOf(
                "issuer" to aliceX500,
                "owner" to bobX500,
                "quantity" to 20.01.toScaledBigDecimal(),
                "observers" to emptyList<String>()
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.burn.BurnTokenFlow\$Initiator"
        )

        val burnTokenFlowResult = awaitRpcFlowFinished(bobHoldingId, burnTokenFlowRequestId)
        assertThat(burnTokenFlowResult.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(burnTokenFlowResult.flowError).isNull()

        val burnTokenResponse = objectMapper.readValue(burnTokenFlowResult.flowResult, BurnTokenResponse::class.java)

        assertThat(burnTokenResponse.burned).isEqualTo(20.01.toScaledBigDecimal())
        assertThat(burnTokenResponse.change).isEqualTo(3.03.toScaledBigDecimal())
    }

    @Test
    fun `fungible contract create command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "VALID"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `fungible contract create command CONTRACT_RULE_CREATE_OUTPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "CONTRACT_RULE_CREATE_OUTPUTS"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains("On fungible state(s) creating, at least one fungible state must be created.")
    }

    @Test
    fun `fungible contract create command CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "CREATE",
                "rule" to "CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains(
            "On fungible state(s) creating, the quantity of every created fungible state must be greater than zero."
        )
    }

    @Test
    fun `fungible contract update command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "VALID"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `fungible contract update command CONTRACT_RULE_UPDATE_INPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_INPUTS"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains("On fungible state(s) updating, at least one fungible state must be consumed.")
    }

    @Test
    fun `fungible contract update command CONTRACT_RULE_UPDATE_OUTPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_OUTPUTS"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains("On fungible state(s) updating, at least one fungible state must be created.")
    }

    @Test
    fun `fungible contract update command CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains("On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.")
    }

    @Test
    fun `fungible contract update command CONTRACT_RULE_UPDATE_SUM fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_SUM"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the " +
                        "unscaled values of the created states."
            )
    }

    @Test
    fun `fungible contract update command CONTRACT_RULE_UPDATE_GROUP_SUM fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "UPDATE",
                "rule" to "CONTRACT_RULE_UPDATE_GROUP_SUM"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the " +
                        "sum of the created states that are fungible with each other."
            )
    }

    @Test
    fun `fungible contract delete command valid`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "VALID"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_SUCCESS)
        assertThat(response.flowError).isNull()
    }

    @Test
    fun `fungible contract delete command CONTRACT_RULE_DELETE_INPUTS fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_INPUTS"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains("On fungible state(s) deleting, at least one fungible state input must be consumed.")
    }

    @Test
    fun `fungible contract delete command CONTRACT_RULE_DELETE_POSITIVE_QUANTITIES fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_POSITIVE_QUANTITIES"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message).contains(
            "On fungible state(s) deleting, the quantity of every created fungible state must be greater than zero."
        )
    }

    @Test
    fun `fungible contract delete command CONTRACT_RULE_DELETE_SUM fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_SUM"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of " +
                        "the unscaled values of the created states."
            )
    }

    @Test
    fun `fungible contract delete command CONTRACT_RULE_DELETE_GROUP_SUM fails`() {
        val request = startRpcFlow(
            aliceHoldingId,
            mapOf(
                "command" to "DELETE",
                "rule" to "CONTRACT_RULE_DELETE_GROUP_SUM"
            ),
            "com.r3.corda.demo.utxo.fungible.workflow.testing.FungibleContractTestFlow"
        )
        val response = awaitRpcFlowFinished(aliceHoldingId, request)
        assertThat(response.flowStatus).isEqualTo(RPC_FLOW_STATUS_FAILED)
        assertThat(response.flowError?.message)
            .contains(
                "On fungible state(s) deleting, the sum of consumed states that are fungible with each other must be greater than the " +
                        "sum of the created states that are fungible with each other."
            )
    }

    data class MintTokenResponse(val balance: Map<String, BigDecimal>)
    data class MoveTokenResponse(val balance: Map<String, BigDecimal>)
    data class BurnTokenResponse(val quantities: Collection<BigDecimal>, val burned: BigDecimal, val change: BigDecimal)
}
