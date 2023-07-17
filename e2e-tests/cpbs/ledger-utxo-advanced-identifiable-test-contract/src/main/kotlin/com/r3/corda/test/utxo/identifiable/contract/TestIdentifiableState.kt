package com.r3.corda.test.utxo.identifiable.contract

import com.r3.corda.ledger.utxo.identifiable.IdentifiableContract
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractCreateCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractDeleteCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableContractUpdateCommand
import com.r3.corda.ledger.utxo.identifiable.IdentifiableState
import net.corda.v5.ledger.utxo.BelongsToContract
import net.corda.v5.ledger.utxo.StateRef
import java.security.PublicKey

@BelongsToContract(TestIdentifiableContract::class)
data class TestIdentifiableState(private val id: StateRef?, private val participants: List<PublicKey>) : IdentifiableState {

    override fun getParticipants(): List<PublicKey> {
        return participants
    }

    override fun getId(): StateRef? {
        return id
    }
}

class TestIdentifiableContract : IdentifiableContract() {

    class Create : IdentifiableContractCreateCommand<TestIdentifiableState>() {
        override fun getContractStateType(): Class<TestIdentifiableState> {
            return TestIdentifiableState::class.java
        }
    }
    class Update: IdentifiableContractUpdateCommand<TestIdentifiableState>() {
        override fun getContractStateType(): Class<TestIdentifiableState> {
            return TestIdentifiableState::class.java
        }
    }
    class Delete: IdentifiableContractDeleteCommand<TestIdentifiableState>() {
        override fun getContractStateType(): Class<TestIdentifiableState> {
            return TestIdentifiableState::class.java
        }
    }

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }
}
