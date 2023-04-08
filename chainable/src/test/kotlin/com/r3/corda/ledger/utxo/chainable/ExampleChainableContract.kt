package com.r3.corda.ledger.utxo.chainable

class ExampleChainableContract : ChainableContract() {

    override fun getPermittedCommandTypes(): List<Class<out ChainableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : ChainableContractCreateCommand<ExampleChainableState>()
    class Update : ChainableContractUpdateCommand<ExampleChainableState>()
    class Delete : ChainableContractDeleteCommand<ExampleChainableState>()
}
