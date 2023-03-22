package com.r3.corda.ledger.utxo.fungible;

import org.jetbrains.annotations.*;

import java.math.*;
import java.util.*;

/**
 * Represents a {@link Numeric} wrapped {@link BigDecimal}.
 */
public final class NumericDecimal implements Numeric<BigDecimal> {

    /**
     * Gets a {@link NumericDecimal} representing the value zero (0).
     */
    @NotNull
    public static final NumericDecimal ZERO = new NumericDecimal(BigDecimal.ZERO);

    /**
     * Gets a {@link NumericDecimal} representing the value one (1).
     */
    @NotNull
    public static final NumericDecimal ONE = new NumericDecimal(BigDecimal.ONE);

    /**
     * Gets a {@link NumericDecimal} representing the value ten (10).
     */
    @NotNull
    public static final NumericDecimal TEN = new NumericDecimal(BigDecimal.TEN);

    /**
     * The underlying {@link BigDecimal} value.
     */
    @NotNull
    private final BigDecimal value;

    /**
     * Initializes a new instance of the {@link NumericDecimal} class.
     *
     * @param value The underlying {@link BigDecimal} value.
     */
    public NumericDecimal(@NotNull final BigDecimal value) {
        this.value = value;
    }

    /**
     * Gets the underlying {@link BigDecimal} value.
     *
     * @return Returns the underlying {@link BigDecimal} value.
     */
    @NotNull
    @Override
    public BigDecimal getValue() {
        return value;
    }

    /**
     * Gets the underlying, unscaled {@link BigDecimal} value.
     *
     * @return Returns the underlying, unscaled {@link BigDecimal} value.
     */
    @NotNull
    @Override
    public BigInteger getUnscaledValue() {
        return getValue().unscaledValue();
    }

    /**
     * Computes the sum of specified value, added to the current value.
     *
     * @param other The other value to add to the current value.
     * @return Returns the sum of specified value, added to the current value.
     */
    @NotNull
    @Override
    public NumericDecimal add(@NotNull final Numeric<BigDecimal> other) {
        if (getValue().scale() != other.getValue().scale()) {
            throw new IllegalArgumentException("Cannot add values with different scales.");
        }

        return new NumericDecimal(getValue().add(other.getValue()));
    }

    /**
     * Computes the difference of the specified value, subtracted from the current value.
     *
     * @param other The other value to subtract from the current value.
     * @return Returns the difference of the specified value, subtracted from the current value.
     */
    @NotNull
    @Override
    public NumericDecimal subtract(@NotNull final Numeric<BigDecimal> other) {
        if (getValue().scale() != other.getValue().scale()) {
            throw new IllegalArgumentException("Cannot add values with different scales.");
        }

        return new NumericDecimal(getValue().subtract(other.getValue()));
    }

    /**
     * Compares this object with the specified object for order.
     *
     * @param other the object to be compared.
     * @return Returns a negative, zero, or positive value indicating the relative order of the current instance, compared to the specified value.
     */
    @Override
    public int compareTo(@NotNull final Numeric<BigDecimal> other) {
        return getValue().compareTo(other.getValue());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    public boolean equals(@NotNull final NumericDecimal other) {
        return getValue().equals(other.getValue());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param obj The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    @Override
    public boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof NumericDecimal && equals((NumericDecimal) obj);
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
