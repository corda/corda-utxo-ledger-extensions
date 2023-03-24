package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.base.annotations.*;
import org.jetbrains.annotations.*;

import java.math.*;

/**
 * Defines a wrapper for {@link Number} types.
 *
 * @param <T> The underlying {@link Number} type.
 */
@DoNotImplement
@CordaSerializable
public interface Numeric<T extends Number> extends Comparable<Numeric<T>> {

    /**
     * Gets the value of the underlying {@link Number}.
     *
     * @return Returns the value of the underlying {@link Number}.
     */
    @NotNull
    T getValue();

    /**
     * Gets the unscaled equivalent of {@link #getValue()}.
     *
     * @return Returns the unscaled equivalent of {@link #getValue()}.
     */
    @NotNull
    BigInteger getUnscaledValue();

    /**
     * Computes the sum of specified value, added to the current value.
     *
     * @param other The other value to add to the current value.
     * @return Returns the sum of specified value, added to the current value.
     */
    @NotNull
    Numeric<T> add(@NotNull Numeric<T> other);

    /**
     * Computes the difference of the specified value, subtracted from the current value.
     *
     * @param other The other value to subtract from the current value.
     * @return Returns the difference of the specified value, subtracted from the current value.
     */
    @NotNull
    Numeric<T> subtract(@NotNull Numeric<T> other);
}
