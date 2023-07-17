package com.r3.corda.test.utxo.issuable.workflow

import net.corda.v5.membership.MemberInfo
import java.security.PublicKey

internal val MemberInfo.firstLedgerKey: PublicKey
    get() = ledgerKeys.first()
