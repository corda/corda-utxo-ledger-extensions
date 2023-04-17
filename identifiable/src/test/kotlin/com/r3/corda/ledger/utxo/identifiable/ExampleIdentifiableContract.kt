package com.r3.corda.ledger.utxo.identifiable

class ExampleIdentifiableContract : IdentifiableContract() {

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : IdentifiableContractCreateCommand<ExampleIdentifiableState>() {
        override fun getContractStateType(): Class<ExampleIdentifiableState> {
            return ExampleIdentifiableState::class.java
        }
    }

    class Update : IdentifiableContractUpdateCommand<ExampleIdentifiableState>() {
        override fun getContractStateType(): Class<ExampleIdentifiableState> {
            return ExampleIdentifiableState::class.java
        }
    }

    class Delete : IdentifiableContractDeleteCommand<ExampleIdentifiableState>() {
        override fun getContractStateType(): Class<ExampleIdentifiableState> {
            return ExampleIdentifiableState::class.java
        }
    }
}
