package com.r3.corda.ledger.utxo.identifiable;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link IdentifiableContract} update commands.
 * <p>
 * This should be implemented by commands intended to update existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
 * <ol>
 *     <li>On identifiable state(s) updating, at least one identifiable state must be consumed.</li>
 *     <li>On identifiable state(s) updating, at least one identifiable state must be created.</li>
 *     <li>On identifiable state(s) updating, each created identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.</li>
 *     <li>On identifiable state(s) updating, only one identifiable state with a matching identifier must be consumed for every created identifiable state with a non-null identifier.</li>
 *     <li>On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null.</li>
 * </ol>
 */
public abstract class IdentifiableContractUpdateCommand<T extends IdentifiableState> extends IdentifiableContractCommand<T> {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull UtxoLedgerTransaction transaction) {
        IdentifiableConstraints.verifyUpdate(transaction, getContractStateType());
        onVerify(transaction);
    }

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    }
}
