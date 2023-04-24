package com.r3.corda.ledger.utxo.identifiable;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link IdentifiableContract} create commands.
 * This should be implemented by commands intended to delete existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
 * <ol>
 *     <li>On identifiable state(s) deleting, at least one identifiable state must be consumed.</li>
 * </ol>
 */
public abstract class IdentifiableContractDeleteCommand<T extends IdentifiableState> extends IdentifiableContractCommand<T> {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull UtxoLedgerTransaction transaction) {
        IdentifiableConstraints.verifyDelete(transaction, getContractStateType());
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
