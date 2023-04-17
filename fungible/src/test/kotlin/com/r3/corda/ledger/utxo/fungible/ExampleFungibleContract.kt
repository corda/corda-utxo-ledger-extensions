package com.r3.corda.ledger.utxo.fungible

class ExampleFungibleContract : FungibleContract() {

    override fun getPermittedCommandTypes(): List<Class<out FungibleContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : FungibleContractCreateCommand<ExampleFungibleState>() {
        override fun getContractStateType(): Class<ExampleFungibleState> {
            return ExampleFungibleState::class.java
        }
    }

    class Update : FungibleContractUpdateCommand<ExampleFungibleState>() {
        override fun getContractStateType(): Class<ExampleFungibleState> {
            return ExampleFungibleState::class.java
        }
    }

    class Delete : FungibleContractDeleteCommand<ExampleFungibleState>() {
        override fun getContractStateType(): Class<ExampleFungibleState> {
            return ExampleFungibleState::class.java
        }
    }
}
