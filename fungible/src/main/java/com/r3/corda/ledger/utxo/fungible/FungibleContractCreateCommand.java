package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link FungibleContract} commands that are intended to create new ledger instances of
 * {@link FungibleState}.
 * <p>
 * This should be implemented by commands intended to create new ledger instances of {@link FungibleState} and will verify the following constraints:
 * <ol>
 *     <li>On fungible state(s) creating, at least one fungible state must be created.</li>
 *     <li>On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.</li>
 * </ol>
 */
public abstract class FungibleContractCreateCommand extends FungibleContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        FungibleConstraints.verifyCreate(transaction);
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
