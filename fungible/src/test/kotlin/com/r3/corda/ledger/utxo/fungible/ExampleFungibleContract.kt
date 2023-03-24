package com.r3.corda.ledger.utxo.fungible

class ExampleFungibleContract : FungibleContract() {

    override fun getPermittedCommandTypes(): List<Class<out FungibleContractCommand>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : FungibleContractCreateCommand()
    class Update : FungibleContractUpdateCommand()
    class Delete : FungibleContractDeleteCommand()
}
