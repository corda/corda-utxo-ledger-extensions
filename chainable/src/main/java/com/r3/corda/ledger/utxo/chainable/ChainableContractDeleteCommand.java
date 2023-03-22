package com.r3.corda.ledger.utxo.chainable;

import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

/**
 * Represents the base class for implementing {@link ChainableContract} create commands.
 * This should be implemented by commands intended to delete existing ledger instances of {@link ChainableState}.
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
        // TODO : Base rules for deleting chainable states...
        onVerify(transaction);
    }

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    }
}
