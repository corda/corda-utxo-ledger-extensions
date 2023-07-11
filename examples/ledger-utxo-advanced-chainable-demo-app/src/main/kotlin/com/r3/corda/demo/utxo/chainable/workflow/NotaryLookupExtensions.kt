package com.r3.corda.demo.utxo.chainable.workflow

import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.membership.NotaryInfo

@Suspendable
internal fun NotaryLookup.getNotaryInfo(name: String): NotaryInfo {
    val memberX500Name = MemberX500Name.parse(name)
    return requireNotNull(lookup(memberX500Name)) {
        "Failed to obtain notary information for the specified name: $name."
    }
}
