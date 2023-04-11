package com.r3.corda.ledger.utxo.chainable;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link ChainableContract} create commands.
 * This should be implemented by commands intended to delete existing ledger instances of {@link ChainableState} and will verify the following constraints:
 * <ol>
 *     <li>On chainable state(s) deleting, at least one chainable state must be consumed.</li>
 * </ol>
 */
public abstract class ChainableContractDeleteCommand extends ChainableContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        ChainableConstraints.verifyDelete(transaction);
        onVerify(transaction);
    }

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    }
}
