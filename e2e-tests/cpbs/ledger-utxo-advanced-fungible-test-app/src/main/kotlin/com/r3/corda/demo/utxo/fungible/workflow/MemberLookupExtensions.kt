package com.r3.corda.demo.utxo.fungible.workflow

import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.membership.MemberInfo
import java.security.PublicKey

@Suspendable
internal fun MemberLookup.getMemberInfo(name: String): MemberInfo {
    val memberX500Name = MemberX500Name.parse(name)
    return requireNotNull(lookup(memberX500Name)) {
        "Failed to obtain member information for the specified name: $name."
    }
}

@Suspendable
internal fun MemberLookup.getMemberX500Name(key: PublicKey): MemberX500Name {
    val memberInfo = requireNotNull(lookup(key)) {
        "Failed to obtain member information for the specified key: $key."
    }

    return memberInfo.name
}
