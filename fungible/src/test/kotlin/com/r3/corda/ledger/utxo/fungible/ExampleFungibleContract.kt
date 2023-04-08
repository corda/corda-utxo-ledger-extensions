package com.r3.corda.ledger.utxo.fungible

class ExampleFungibleContract : FungibleContract() {

    override fun getPermittedCommandTypes(): List<Class<out FungibleContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : FungibleContractCreateCommand<ExampleFungibleState>()
    class Update : FungibleContractUpdateCommand<ExampleFungibleState>()
    class Delete : FungibleContractDeleteCommand<ExampleFungibleState>()
}
