package net.cordapp.demo.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.cordapp.demo.utxo.fungible.contract.Token
import java.security.PublicKey

@Suspendable
internal fun UtxoLedgerService.getAvailableTokens(
    issuerKeys: Iterable<PublicKey>,
    ownerKeys: Iterable<PublicKey>,
    targetQuantity: NumericDecimal
): List<StateAndRef<Token>> {
    return findUnconsumedStatesByType(Token::class.java)
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