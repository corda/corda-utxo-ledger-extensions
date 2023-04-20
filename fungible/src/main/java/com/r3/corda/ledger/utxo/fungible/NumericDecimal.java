package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.base.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a {@link Numeric} wrapped {@link BigDecimal}.
 */
public final class NumericDecimal implements Numeric<BigDecimal> {

    /**
     * Gets a {@link NumericDecimal} representing the value zero (0).
     */
    public static final NumericDecimal ZERO = new NumericDecimal(BigDecimal.ZERO, 0);

    /**
     * Gets a {@link NumericDecimal} representing the value one (1).
     */
    public static final NumericDecimal ONE = new NumericDecimal(BigDecimal.ONE, 0);

    /**
     * Gets a {@link NumericDecimal} representing the value ten (10).
     */
    public static final NumericDecimal TEN = new NumericDecimal(BigDecimal.TEN, 0);

    /**
     * The underlying {@link BigDecimal} value.
     */
    @NotNull
    private final BigDecimal value;

    /**
     * Initializes a new instance of the {@link NumericDecimal} class.
     *
     * @param value The underlying {@link BigDecimal} value.
     * @param scale The scale of the underlying {@link BigDecimal} value.
     *              The default {@link RoundingMode} when setting the scale is {@link RoundingMode#UNNECESSARY}.
     */
    public NumericDecimal(@NotNull final BigDecimal value, final int scale) {
        this(value, scale, RoundingMode.UNNECESSARY);
    }

    /**
     * Initializes a new instance of the {@link NumericDecimal} class.
     *
     * @param value The underlying {@link BigDecimal} value.
     * @param scale The scale of the underlying {@link BigDecimal} value.
     * @param mode  The {@link RoundingMode} to use when setting the scale of the underlying {@link BigDecimal} value.
     */
    public NumericDecimal(@NotNull final BigDecimal value, final int scale, final RoundingMode mode) {
        this.value = value.setScale(scale, mode);
    }

    /**
     * Initializes a new instance of the {@link NumericDecimal} class.
     *
     * @param value The underlying {@link BigDecimal} value.
     */
    @ConstructorForDeserialization
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
     * Sets the scale of the underlying {@link BigDecimal} value.
     *
     * @param scale The scale of the underlying {@link BigDecimal} value.
     *              The default {@link RoundingMode} when setting the scale is {@link RoundingMode#UNNECESSARY}.
     * @return Returns a new {@link NumericDecimal} with the specified scale.
     */
    @NotNull
    @SuppressWarnings("unused")
    public NumericDecimal setScale(final int scale) {
        return setScale(scale, RoundingMode.UNNECESSARY);
    }

    /**
     * Sets the scale of the underlying {@link BigDecimal} value.
     *
     * @param scale The scale of the underlying {@link BigDecimal} value.
     * @param mode  The {@link RoundingMode} to use when setting the scale of the underlying {@link BigDecimal} value.
     * @return Returns a new {@link NumericDecimal} with the specified scale.
     */
    @NotNull
    @SuppressWarnings("unused")
    public NumericDecimal setScale(final int scale, RoundingMode mode) {
        return new NumericDecimal(getValue(), scale, mode);
    }

    /**
     * Computes the sum of specified value, added to the current value.
     *
     * @param other The other value to add to the current value.
     * @return Returns the sum of specified value, added to the current value.
     */
    @NotNull
    @Override
    public NumericDecimal plus(@NotNull final Numeric<BigDecimal> other) {
        if (getValue().scale() != other.getValue().scale()) {
            throw new IllegalArgumentException("Cannot add values with different scales.");
        }

        return new NumericDecimal(getValue().add(other.getValue()), getValue().scale());
    }

    /**
     * Computes the difference of the specified value, subtracted from the current value.
     *
     * @param other The other value to subtract from the current value.
     * @return Returns the difference of the specified value, subtracted from the current value.
     */
    @NotNull
    @Override
    public NumericDecimal minus(@NotNull final Numeric<BigDecimal> other) {
        if (getValue().scale() != other.getValue().scale()) {
            throw new IllegalArgumentException("Cannot subtract values with different scales.");
        }

        return new NumericDecimal(getValue().subtract(other.getValue()), getValue().scale());
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
        return compareTo(other) == 0;
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
