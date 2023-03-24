package com.r3.corda.ledger.utxo.identifiable

class ExampleIdentifiableContract : IdentifiableContract() {

    override fun getPermittedCommandTypes(): List<Class<out IdentifiableContractCommand>> {
        return listOf(Create::class.java, Update::class.java, Delete::class.java)
    }

    class Create : IdentifiableContractCreateCommand()
    class Update : IdentifiableContractUpdateCommand()
    class Delete : IdentifiableContractDeleteCommand()
}
