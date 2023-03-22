package com.r3.corda.ledger.utxo.fungible

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Creates a wrapped [NumericInteger] from the current [BigInteger].
 *
 * @receiver The current [BigInteger] from which to create a wrapped [NumericInteger].
 * @return Returns a wrapped [NumericInteger] from the current [BigInteger].
 */
fun BigInteger.toNumeric(): NumericInteger {
    return NumericInteger(this)
}

/**
 * Creates a wrapped [NumericDecimal] from the current [BigDecimal].
 *
 * @receiver The current [BigDecimal] from which to create a wrapped [NumericDecimal].
 * @return Returns a wrapped [NumericDecimal] from the current [BigDecimal].
 */
fun BigDecimal.toNumeric(): NumericDecimal {
    return NumericDecimal(this)
}

/**
 * Obtains the sum of all the [BigInteger] values in the current [Iterable] collection.
 *
 * @receiver The current [Iterable] collection of [BigInteger] values to sum.
 * @return Returns the sum of all the [BigInteger] values in the current [Iterable] collection.
 */
fun Iterable<BigInteger>.sum(): BigInteger {
    return fold(BigInteger.ZERO, BigInteger::add)
}

/**
 * Obtains the sum of all the [NumericInteger] values in the current [Iterable] collection.
 *
 * @receiver The current [Iterable] collection of [NumericInteger] values to sum.
 * @return Returns the sum of all the [NumericInteger] values in the current [Iterable] collection.
 */
fun Iterable<NumericInteger>.sum(): NumericInteger {
    return fold(NumericInteger.ZERO, NumericInteger::add)
}

/**
 * Obtains the sum of all the [NumericDecimal] values in the current [Iterable] collection.
 *
 * @receiver The current [Iterable] collection of [NumericDecimal] values to sum.
 * @return Returns the sum of all the [NumericDecimal] values in the current [Iterable] collection.
 */
fun Iterable<NumericDecimal>.sum(): NumericDecimal {
    return fold(NumericDecimal.ZERO, NumericDecimal::add)
}
