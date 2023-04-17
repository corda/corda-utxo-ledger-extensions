package com.r3.corda.ledger.utxo.testing

import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.crypto.SecureHash
import net.corda.v5.ledger.utxo.StateRef
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import java.security.PublicKey

@TransactionBuilderDslMarker
fun buildTransaction(notaryKey: PublicKey, notaryName: MemberX500Name, action: TransactionBuilderDsl.() -> Unit): UtxoLedgerTransaction {
    return TransactionBuilderDsl(notaryKey, notaryName).apply(action).toLedgerTransaction()
}

@TransactionBuilderDslMarker
fun randomSecureHash(): SecureHash {
    return ContractTestUtils.createRandomSecureHash()
}

@TransactionBuilderDslMarker
fun randomStateRef(): StateRef {
    return ContractTestUtils.createRandomStateRef()
}
