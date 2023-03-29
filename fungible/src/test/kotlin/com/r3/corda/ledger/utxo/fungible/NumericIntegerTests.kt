package com.r3.corda.ledger.utxo.fungible

import org.junit.jupiter.api.Test
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericIntegerTests {

    @Test
    fun `NumericInteger_getValue should return the expected result`() {

        // Arrange
        val value = NumericInteger(BigInteger.valueOf(12345))
        val expected = BigInteger.valueOf(12345)

        // Act
        val actual = value.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericInteger_getUnscaledValue should return the expected result`() {

        // Arrange
        val value = NumericInteger(BigInteger.valueOf(12345))
        val expected = BigInteger.valueOf(12345)

        // Act
        val actual = value.unscaledValue

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericInteger_add should return the expected result`() {

        // Arrange
        val left = NumericInteger(BigInteger.valueOf(12345))
        val right = NumericInteger(BigInteger.valueOf(98765))
        val expected = NumericInteger(BigInteger.valueOf(111110))

        // Act
        val actual = left.plus(right)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericInteger_subtract should return the expected result`() {

        // Arrange
        val left = NumericInteger(BigInteger.valueOf(98765))
        val right = NumericInteger(BigInteger.valueOf(12345))
        val expected = NumericInteger(BigInteger.valueOf(86420))

        // Act
        val actual = left.minus(right)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericInteger_compareTo should return a value of 1 when the left-hand value is greater than the right-hand value`() {

        // Arrange
        val left = NumericInteger(BigInteger.TEN)
        val right = NumericInteger(BigInteger.ONE)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(1, actual)
    }

    @Test
    fun `NumericInteger_compareTo should return a value of -1 when the left-hand value is less than the right-hand value`() {

        // Arrange
        val left = NumericInteger(BigInteger.ONE)
        val right = NumericInteger(BigInteger.TEN)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(-1, actual)
    }

    @Test
    fun `NumericInteger_compareTo should return a value of 0 when the left-hand value is equal to the right-hand value`() {

        // Arrange
        val left = NumericInteger(BigInteger.TEN)
        val right = NumericInteger(BigInteger.TEN)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(0, actual)
    }

    @Test
    fun `NumericInteger_equals should return true if the left-hand value is equal to the right-hand value`() {

        // Arrange
        val left = NumericInteger(BigInteger.TEN)
        val right = NumericInteger(BigInteger.TEN)

        // Act
        val actual = left.equals(right)

        // Assert
        assertTrue(actual)
    }

    @Test
    fun `NumericInteger_equals should return false if the left-hand value is not equal to the right-hand value`() {

        // Arrange
        val left = NumericInteger(BigInteger.TEN)
        val right = NumericInteger(BigInteger.ONE)

        // Act
        val actual = left.equals(right)

        // Assert
        assertFalse(actual)
    }
}
