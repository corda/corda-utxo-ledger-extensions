package com.r3.corda.ledger.utxo.chainable;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link ChainableContract} create commands.
 * This should be implemented by commands intended to create new ledger instances of {@link ChainableState} and will verify the following constraints:
 * <ol>
 *     <li>On chainable state(s) creating, zero chainable states must be consumed.</li>
 *     <li>On chainable state(s) creating, at least one chainable state must be created.</li>
 *     <li>On chainable state(s) creating, the previous state pointer of every created chainable state must be null</li>
 * </ol>
 */
public abstract class ChainableContractCreateCommand extends ChainableContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        ChainableConstraints.verifyCreate(transaction);
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