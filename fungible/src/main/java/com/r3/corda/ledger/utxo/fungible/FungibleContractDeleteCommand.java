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
 *     <li>On fungible state(s) deleting, the quantity of every created fungible state must be greater than zero.</li>
 *     <li>On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.</li>
 *     <li>On fungible state(s) deleting, the sum of consumed states that are fungible with each other must be greater than the sum of the created states that are fungible with each other.</li>
 * </ul>
 */
public abstract class FungibleContractDeleteCommand<T extends FungibleState<?>> extends FungibleContractCommand<T> {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        FungibleConstraints.verifyDelete(transaction, getContractStateType());
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
