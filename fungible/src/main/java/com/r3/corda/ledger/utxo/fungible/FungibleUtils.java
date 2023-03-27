package com.r3.corda.ledger.utxo.fungible;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

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
     * @return Returns the sum of all the specified {@link FungibleState} instances.
     */
    @NotNull
    @SuppressWarnings("rawtypes")
    public static BigInteger sum(@NotNull final List<FungibleState> states) {
        BigInteger result = BigInteger.ZERO;

        for (final FungibleState state : states) {
            result = result.add(state.getQuantity().getUnscaledValue());
        }

        return result;
    }
}
