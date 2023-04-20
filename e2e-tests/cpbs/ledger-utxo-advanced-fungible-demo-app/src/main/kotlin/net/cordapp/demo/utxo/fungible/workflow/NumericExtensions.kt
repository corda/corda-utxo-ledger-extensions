package net.cordapp.demo.utxo.fungible.workflow

import com.r3.corda.ledger.utxo.fungible.NumericDecimal
import java.math.BigDecimal

internal fun Iterable<BigDecimal>.sum(): BigDecimal {
    val scale = map { it.scale() }.distinct().single()
    return fold(BigDecimal.ZERO.setScale(scale), BigDecimal::add)
}

internal fun Iterable<NumericDecimal>.sum(): NumericDecimal {
    val scale = map { it.value.scale() }.distinct().single()
    return fold(NumericDecimal.ZERO.setScale(scale), NumericDecimal::plus)
}