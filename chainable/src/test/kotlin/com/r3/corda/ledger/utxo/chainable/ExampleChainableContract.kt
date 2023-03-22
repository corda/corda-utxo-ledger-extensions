package com.r3.corda.ledger.utxo.chainable

class ExampleChainableContract : ChainableContract() {

    override fun getPermittedCommandTypes(): List<Class<out ChainableContractCommand>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : ChainableContractCreateCommand()
    class Update : ChainableContractUpdateCommand()
    class Delete : ChainableContractDeleteCommand()
    class DisallowedCommand : ChainableContractCreateCommand()
}
