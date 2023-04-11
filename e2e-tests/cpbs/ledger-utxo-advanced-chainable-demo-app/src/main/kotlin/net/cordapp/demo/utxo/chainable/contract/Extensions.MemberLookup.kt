package net.cordapp.demo.utxo.chainable.contract

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.membership.MemberInfo

@Suspendable
internal fun MemberLookup.getMemberInfo(name: String): MemberInfo {
    val memberX500Name = MemberX500Name.parse(name)
    return requireNotNull(lookup(memberX500Name)) {
        "Failed to obtain member information for the specified name: $name."
    }
}
