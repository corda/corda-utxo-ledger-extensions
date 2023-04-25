package com.r3.corda.demo.utxo.issuable.contract

import com.r3.corda.ledger.utxo.issuable.IssuableState
import com.r3.corda.ledger.utxo.issuable.WellKnownIssuableState
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import java.security.PublicKey

@BelongsToContract(TestIssuableContract::class)
data class TestIssuableState(
    private val issuer: PublicKey,
    private val issuerName: MemberX500Name,
    private val participants: List<PublicKey>
) : IssuableState, WellKnownIssuableState {

    override fun getParticipants(): List<PublicKey> {
        return participants
    }

    override fun getIssuer(): PublicKey {
        return issuer
    }

    override fun getIssuerName(): MemberX500Name {
       return issuerName
    }
}

class TestIssuableContract : Contract {

    class Create : Command
    class Update : Command
    class Delete : Command

    override fun verify(transaction: UtxoLedgerTransaction) {
        // all good
    }
}
