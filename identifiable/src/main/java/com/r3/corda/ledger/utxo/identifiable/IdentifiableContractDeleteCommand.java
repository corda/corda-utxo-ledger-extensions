package com.r3.corda.ledger.utxo.identifiable;

import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

/**
 * Represents the base class for implementing {@link IdentifiableContract} create commands.
 * This should be implemented by commands intended to delete existing ledger instances of {@link IdentifiableState}.
 */
public abstract class IdentifiableContractDeleteCommand extends IdentifiableContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull UtxoLedgerTransaction transaction) {
        // TODO : Base rules for deleting identifiable states...
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
