package com.r3.corda.demo.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import com.r3.corda.ledger.utxo.ownable.query.OwnableStateQueries
import net.corda.v5.application.crypto.DigestService
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.crypto.DigestAlgorithmName
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import com.r3.corda.demo.utxo.fungible.contract.Token
import java.security.PublicKey
import java.time.Instant

@Suspendable
internal fun UtxoLedgerService.getAvailableTokens(
    issuerKeys: Iterable<PublicKey>,
    ownerKeys: Iterable<PublicKey>,
    targetQuantity: NumericDecimal,
    digestService: DigestService
): List<StateAndRef<Token>> {
    return query(OwnableStateQueries.GET_BY_OWNER, StateAndRef::class.java)
        .setCreatedTimestampLimit(Instant.now())
        .setLimit(50)
        .setParameter("owner", digestService.hash(ownerKeys.single().encoded, DigestAlgorithmName.SHA2_256).toString())
        .setParameter("stateType", Token::class.java.name)
        .execute()
        .results
        .filterIsInstance<StateAndRef<Token>>()
        .filter { it.state.contractState.issuer in issuerKeys }
        .filter { it.state.contractState.owner in ownerKeys }
        .sortedBy { it.state.contractState.quantity }
        .apply { checkSufficientTokenBalance(this, targetQuantity) }
}

private fun checkSufficientTokenBalance(availableTokens: Iterable<StateAndRef<Token>>, targetQuantity: NumericDecimal) {
    val availableQuantity = availableTokens.map { it.state.contractState.quantity }.sum()
    check(availableQuantity >= targetQuantity) {
        "Insufficient token balance available to perform move operation: Target = $targetQuantity, Available = $availableQuantity."
    }
}
