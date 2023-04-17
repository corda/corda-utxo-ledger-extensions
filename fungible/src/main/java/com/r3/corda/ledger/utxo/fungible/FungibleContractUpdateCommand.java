package com.r3.corda.ledger.utxo.fungible;

import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the base class for implementing {@link FungibleContract} commands that are intended to update existing ledger instances of
 * {@link FungibleState}.
 * <p>
 * This should be implemented by commands intended to update existing ledger instances of {@link FungibleState} and will verify the following constraints:
 * <ol>
 *  <li>On fungible state(s) updating, at least one fungible state must be consumed.</li>
 *  <li>On fungible state(s) updating, at least one fungible state must be created.</li>
 *  <li>On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.</li>
 *  <li>On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.</li>
 *  <li>On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.</li>
 * </ol>
 */
public abstract class FungibleContractUpdateCommand<T extends FungibleState<?>> extends FungibleContractCommand<T> {

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        FungibleConstraints.verifyUpdate(transaction, getContractStateType());
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
