package com.r3.corda.ledger.utxo.ownable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents verification constraints for creating and deleting {@link OwnableState} instances.
 */
public final class OwnableConstraints {

    static final String CONTRACT_RULE_UPDATE_SIGNATORIES =
            "On ownable state(s) updating, the owner of every consumed ownable state must sign the transaction.";

    /**
     * Prevents instances of {@link OwnableConstraints} from being created.
     */
    private OwnableConstraints() {
    }

    /**
     * Verifies {@link OwnableState} inputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link OwnableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On ownable state(s) updating, the owner of every consumed ownable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link OwnableState} to verify.
     * @param <T>         The underlying type of {@link OwnableState} to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends OwnableState> void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        List<T> inputs = transaction.getInputStates(type);

        Check.all(inputs, it -> transaction.getSignatories().contains(it.getOwner()), CONTRACT_RULE_UPDATE_SIGNATORIES);
    }

    /**
     * Verifies {@link OwnableState} inputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link OwnableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On ownable state(s) updating, the owner of every consumed ownable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyUpdate(transaction, OwnableState.class);
    }
}
