package com.r3.corda.ledger.utxo.chainable

class ExampleChainableContract : ChainableContract() {

    override fun getPermittedCommandTypes(): List<Class<out ChainableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : ChainableContractCreateCommand<ExampleChainableState>() {
        override fun getContractStateType(): Class<ExampleChainableState> {
            return ExampleChainableState::class.java
        }
    }

    class Update : ChainableContractUpdateCommand<ExampleChainableState>() {
        override fun getContractStateType(): Class<ExampleChainableState> {
            return ExampleChainableState::class.java
        }
    }

    class Delete : ChainableContractDeleteCommand<ExampleChainableState>() {
        override fun getContractStateType(): Class<ExampleChainableState> {
            return ExampleChainableState::class.java
        }
    }
}
