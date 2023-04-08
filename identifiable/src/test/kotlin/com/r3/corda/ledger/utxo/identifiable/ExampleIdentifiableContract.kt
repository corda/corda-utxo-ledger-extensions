package com.r3.corda.ledger.utxo.identifiable

class ExampleIdentifiableContract : IdentifiableContract() {

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand<*>>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : IdentifiableContractCreateCommand<ExampleIdentifiableState>()
    class Update : IdentifiableContractUpdateCommand<ExampleIdentifiableState>()
    class Delete : IdentifiableContractDeleteCommand<ExampleIdentifiableState>()
}
