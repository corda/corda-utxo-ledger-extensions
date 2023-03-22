package com.r3.corda.ledger.utxo.testing

import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.common.Party
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction

@TransactionBuilderDslMarker
fun buildTransaction(notary: Party, action: TransactionBuilderDsl.() -> Unit): UtxoLedgerTransaction {
    return TransactionBuilderDsl(notary).apply(action).toLedgerTransaction()
}

@TransactionBuilderDslMarker
fun randomSecureHash(): SecureHash {
    return ContractTestUtils.createRandomSecureHash()
}

@TransactionBuilderDslMarker
fun randomStateRef(): StateRef {
    return ContractTestUtils.createRandomStateRef()
}
