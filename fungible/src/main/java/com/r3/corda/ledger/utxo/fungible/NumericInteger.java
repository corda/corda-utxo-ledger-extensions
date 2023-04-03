package com.r3.corda.ledger.utxo.fungible;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Represents a {@link Numeric} wrapped {@link BigInteger}.
 */
public final class NumericInteger implements Numeric<BigInteger> {

    /**
     * Gets a {@link NumericInteger} representing the value zero (0).
     */
    public static final NumericInteger ZERO = new NumericInteger(BigInteger.ZERO);

    /**
     * Gets a {@link NumericInteger} representing the value one (1).
     */
    public static final NumericInteger ONE = new NumericInteger(BigInteger.ONE);

    /**
     * Gets a {@link NumericInteger} representing the value ten (10).
     */
    public static final NumericInteger TEN = new NumericInteger(BigInteger.TEN);

    /**
     * The underlying {@link BigInteger} value.
     */
    @NotNull
    private final BigInteger value;

    /**
     * Initializes a new instance of the {@link NumericInteger} class.
     *
     * @param value The underlying {@link BigInteger} value.
     */
    public NumericInteger(@NotNull final BigInteger value) {
        this.value = value;
    }

    /**
     * Gets the underlying {@link BigInteger} value.
     *
     * @return Returns the underlying {@link BigInteger} value.
     */
    @NotNull
    @Override
    public BigInteger getValue() {
        return value;
    }

    /**
     * Gets the underlying, unscaled {@link BigInteger} value.
     *
     * @return Returns the underlying, unscaled {@link BigInteger} value.
     */
    @NotNull
    @Override
    public BigInteger getUnscaledValue() {
        return getValue();
    }

    /**
     * Computes the sum of specified value, added to the current value.
     *
     * @param other The other value to add to the current value.
     * @return Returns the sum of specified value, added to the current value.
     */
    @NotNull
    @Override
    public NumericInteger plus(@NotNull final Numeric<BigInteger> other) {
        return new NumericInteger(getValue().add(other.getValue()));
    }

    /**
     * Computes the difference of the specified value, subtracted from the current value.
     *
     * @param other The other value to subtract from the current value.
     * @return Returns the difference of the specified value, subtracted from the current value.
     */
    @NotNull
    @Override
    public NumericInteger minus(@NotNull final Numeric<BigInteger> other) {
        return new NumericInteger(getValue().subtract(other.getValue()));
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param other the object to be compared.
     * @return Returns a negative, zero, or positive value indicating the relative order of the current instance, compared to the specified value.
     */
    @Override
    public int compareTo(@NotNull final Numeric<BigInteger> other) {
        return getValue().compareTo(other.getValue());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    public boolean equals(@NotNull final NumericInteger other) {
        return getValue().equals(other.getValue());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param obj The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof NumericInteger && equals((NumericInteger) obj);
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    @Override
    public String toString() {
        return getValue().toString();
    }
}
