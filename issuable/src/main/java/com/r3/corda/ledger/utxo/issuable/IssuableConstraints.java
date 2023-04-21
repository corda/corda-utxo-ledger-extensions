package com.r3.corda.ledger.utxo.issuable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents verification constraints for creating and deleting {@link IssuableState} instances.
 */
public final class IssuableConstraints {

    static final String CONTRACT_RULE_CREATE_SIGNATORIES =
            "On issuable state(s) creating, the issuer of every created issuable state must sign the transaction.";

    static final String CONTRACT_RULE_DELETE_SIGNATORIES =
            "On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction.";

    /**
     * Prevents instances of {@link IssuableConstraints} from being created.
     */
    private IssuableConstraints() {
    }

    /**
     * Verifies {@link IssuableState} outputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link IssuableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On issuable state(s) creating, the issuer of every created issuable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link IssuableState} to verify.
     * @param <T>         The underlying type of {@link IssuableState} to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends IssuableState> void verifyCreate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        List<T> outputs = transaction.getOutputStates(type);

        Check.all(outputs, it -> transaction.getSignatories().contains(it.getIssuer()), CONTRACT_RULE_CREATE_SIGNATORIES);
    }

    /**
     * Verifies {@link IssuableState} outputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link IssuableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On issuable state(s) creating, the issuer of every created issuable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyCreate(transaction, IssuableState.class);
    }

    /**
     * Verifies {@link IssuableState} inputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to delete existing ledger instances of {@link IssuableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link IssuableState} to verify.
     * @param <T>         The underlying type of {@link IssuableState} to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends IssuableState> void verifyDelete(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        List<T> inputs = transaction.getInputStates(type);

        Check.all(inputs, it -> transaction.getSignatories().contains(it.getIssuer()), CONTRACT_RULE_DELETE_SIGNATORIES);
    }

    /**
     * Verifies {@link IssuableState} inputs of the specified type, which are contained in the specified {@link UtxoLedgerTransaction}.
     * <p>
     * This should be implemented by commands intended to delete existing ledger instances of {@link IssuableState} and will verify the following constraints:
     * </p>
     * <ol>
     *     <li>On issuable state(s) deleting, the issuer of every consumed issuable state must sign the transaction.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        verifyCreate(transaction, IssuableState.class);
    }
}
