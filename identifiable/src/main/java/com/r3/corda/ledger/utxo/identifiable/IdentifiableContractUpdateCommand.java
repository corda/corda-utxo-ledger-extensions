package com.r3.corda.ledger.utxo.identifiable;

import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

/**
 * Represents the base class for implementing {@link IdentifiableContract} update commands.
 * This should be implemented by commands intended to update existing ledger instances of {@link IdentifiableState}.
 */
public abstract class IdentifiableContractUpdateCommand extends IdentifiableContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull UtxoLedgerTransaction transaction) {
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
