package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link FungibleContract} commands that are intended to delete (consume) existing ledger
 * instances of {@link FungibleState}.
 * <p>
 * This command will ensure that:
 * <ul>
 *     <li>On fungible state(s) deleting, at least one fungible state input must be consumed.</li>
 *     <li>On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.</li>
 * </ul>
 */
public abstract class FungibleContractDeleteCommand extends FungibleContractCommand {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        FungibleConstraints.verifyDelete(transaction);
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