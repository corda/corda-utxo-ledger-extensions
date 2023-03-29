package com.r3.corda.ledger.utxo.fungible

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericDecimalTests {

    @Test
    fun `NumericDecimal_getValue should return the expected result`() {

        // Arrange
        val value = NumericDecimal(123.45.toBigDecimal())
        val expected = 123.45.toBigDecimal()

        // Act
        val actual = value.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_getUnscaledValue should return the expected result`() {

        // Arrange
        val value = NumericDecimal(123.45.toBigDecimal())
        val expected = BigInteger.valueOf(12345)

        // Act
        val actual = value.unscaledValue

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_plus should return the expected result`() {

        // Arrange
        val left = NumericDecimal(123.45.toBigDecimal())
        val right = NumericDecimal(678.99.toBigDecimal())
        val expected = NumericDecimal(802.44.toBigDecimal())

        // Act
        val actual = left.plus(right)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_plus should return the expected result (kotlin)`() {

        // Arrange
        val left = NumericDecimal(123.45.toBigDecimal())
        val right = NumericDecimal(678.99.toBigDecimal())
        val expected = NumericDecimal(802.44.toBigDecimal())

        // Act
        val actual = left + right

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_add throw an IllegalArgumentException when trying to add values with different scales`() {

        // Arrange
        val left = NumericDecimal(123.456.toBigDecimal())
        val right = NumericDecimal(78.9.toBigDecimal())

        // Act
        val exception = assertThrows<IllegalArgumentException> { left.plus(right) }

        // Assert
        assertEquals("Cannot add values with different scales.", exception.message)
    }

    @Test
    fun `NumericDecimal_minus should return the expected result`() {

        // Arrange
        val left = NumericDecimal(678.99.toBigDecimal())
        val right = NumericDecimal(123.45.toBigDecimal())
        val expected = NumericDecimal(555.54.toBigDecimal())

        // Act
        val actual = left.minus(right)

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_minus should return the expected result (kotlin)`() {

        // Arrange
        val left = NumericDecimal(678.99.toBigDecimal())
        val right = NumericDecimal(123.45.toBigDecimal())
        val expected = NumericDecimal(555.54.toBigDecimal())

        // Act
        val actual = left - right

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `NumericDecimal_subtract throw an IllegalArgumentException when trying to subtract values with different scales`() {

        // Arrange
        val left = NumericDecimal(123.456.toBigDecimal())
        val right = NumericDecimal(78.9.toBigDecimal())

        // Act
        val exception = assertThrows<IllegalArgumentException> { left.minus(right) }

        // Assert
        assertEquals("Cannot subtract values with different scales.", exception.message)
    }

    @Test
    fun `NumericDecimal_compareTo should return a value of 1 when the left-hand value is greater than the right-hand value`() {

        // Arrange
        val left = NumericDecimal(BigDecimal.TEN)
        val right = NumericDecimal(BigDecimal.ONE)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(1, actual)
    }

    @Test
    fun `NumericDecimal_compareTo should return a value of -1 when the left-hand value is less than the right-hand value`() {

        // Arrange
        val left = NumericDecimal(BigDecimal.ONE)
        val right = NumericDecimal(BigDecimal.TEN)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(-1, actual)
    }

    @Test
    fun `NumericDecimal_compareTo should return a value of 0 when the left-hand value is equal to the right-hand value`() {

        // Arrange
        val left = NumericDecimal(BigDecimal.TEN)
        val right = NumericDecimal(BigDecimal.TEN)

        // Act
        val actual = left.compareTo(right)

        // Assert
        assertEquals(0, actual)
    }

    @Test
    fun `NumericDecimal_equals should return true if the left-hand value is equal to the right-hand value`() {

        // Arrange
        val left = NumericDecimal(BigDecimal.TEN)
        val right = NumericDecimal(BigDecimal.TEN)

        // Act
        val actual = left.equals(right)

        // Assert
        assertTrue(actual)
    }

    @Test
    fun `NumericDecimal_equals should return false if the left-hand value is not equal to the right-hand value`() {

        // Arrange
        val left = NumericDecimal(BigDecimal.TEN)
        val right = NumericDecimal(BigDecimal.ONE)

        // Act
        val actual = left.equals(right)

        // Assert
        assertFalse(actual)
    }
}
