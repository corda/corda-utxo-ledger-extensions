package com.r3.corda.demo.utxo.ownable.contract

import com.r3.corda.ledger.utxo.ownable.OwnableConstraints
import com.r3.corda.ledger.utxo.ownable.OwnableState
import com.r3.corda.ledger.utxo.ownable.WellKnownOwnableState
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.Command
import net.corda.v5.ledger.utxo.Contract
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import java.security.PublicKey

@BelongsToContract(MyOwnableContract::class)
data class MyOwnableState(
    private val owner: PublicKey,
    private val ownerName: MemberX500Name,
    private val participants: List<PublicKey>
) : OwnableState, WellKnownOwnableState {

    override fun getParticipants(): List<PublicKey> {
        return participants
    }

    override fun getOwner(): PublicKey {
        return owner
    }

    override fun getOwnerName(): MemberX500Name {
       return ownerName
    }
}

class MyOwnableContract : Contract {

    class Create : Command
    class Update : Command

    override fun verify(transaction: UtxoLedgerTransaction) {
        val commands = transaction.commands
        if (commands.any { it::class.java == Update::class.java }) {
            OwnableConstraints.verifyUpdate(transaction)
        }
    }
}
