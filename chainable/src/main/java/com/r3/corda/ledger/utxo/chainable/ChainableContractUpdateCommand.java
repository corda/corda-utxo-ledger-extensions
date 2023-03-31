package com.r3.corda.ledger.utxo.chainable;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link ChainableContract} update commands.
 * This should be implemented by commands intended to update existing ledger instances of {@link ChainableState}.
 */
public abstract class ChainableContractUpdateCommand extends ChainableContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public void verify(@NotNull final UtxoLedgerTransaction transaction) {
        ChainableConstraints.verifyUpdate(transaction);
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
