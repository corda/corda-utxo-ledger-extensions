package com.r3.corda.ledger.utxo.fungible;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * Represents fungible utilities.
 */
final class FungibleUtils {

    /**
     * Prevents new instances of {@link FungibleUtils} from being initialized.
     */
    private FungibleUtils() {
    }

    /**
     * Computes the sum of all the specified {@link FungibleState} instances.
     *
     * @param states The {@link FungibleState} instances to sum.
     * @param <T>    The underlying {@link FungibleState} type to sum.
     * @return Returns the sum of all the specified {@link FungibleState} instances.
     */
    @NotNull
    public static <T extends FungibleState<?>> BigInteger sum(@NotNull final Iterable<T> states) {
        BigInteger result = BigInteger.ZERO;

        for (final T state : states) {
            result = result.add(state.getQuantity().getUnscaledValue());
        }

        return result;
    }
}
