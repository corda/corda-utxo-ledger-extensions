package com.r3.corda.demo.utxo.fungible.contract

import com.r3.corda.ledger.utxo.base.VisibleState
import com.r3.corda.ledger.utxo.fungible.FungibleState
import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import com.r3.corda.ledger.utxo.issuable.WellKnownIssuableState
import com.r3.corda.ledger.utxo.ownable.OwnableState
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.VisibilityChecker
import java.security.PublicKey

@BelongsToContract(TokenContract::class)
data class Token(
    private val issuerName: MemberX500Name,
    private val issuer: PublicKey,
    private val owner: PublicKey,
    private val quantity: NumericDecimal
) : FungibleState<NumericDecimal>, WellKnownIssuableState, OwnableState, VisibleState {

    override fun getIssuerName(): MemberX500Name {
        return issuerName
    }

    override fun getIssuer(): PublicKey {
        return issuer
    }

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getParticipants(): List<PublicKey> {
        return listOf(owner)
    }

    override fun getQuantity(): NumericDecimal {
        return quantity
    }

    override fun isFungibleWith(other: FungibleState<NumericDecimal>): Boolean {
        return other is Token && other.issuer == issuer
    }

    @Suspendable
    override fun isVisible(checker: VisibilityChecker): Boolean {
        return super.isVisible(checker) || checker.containsMySigningKeys(listOf(issuer))
    }
}
