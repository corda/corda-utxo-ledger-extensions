package com.r3.corda.ledger.utxo.e2etest

import net.corda.e2etest.utilities.CODE_SIGNER_CERT
import net.corda.e2etest.utilities.assertWithRetry
import net.corda.e2etest.utilities.cluster
import net.corda.utilities.seconds

private val retryTimeout = 120.seconds
private val retryInterval = 1.seconds

fun uploadTrustedCertificate() {
    cluster {
        assertWithRetry {
            // Certificate upload can be slow in the combined worker, especially after it has just started up.
            timeout(retryTimeout)
            interval(retryInterval)
            command { importCertificate(CODE_SIGNER_CERT, "code-signer", "cordadev") }
            condition { it.code == 204 }
        }
    }
}
