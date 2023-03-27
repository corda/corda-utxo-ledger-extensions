package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

    final static String CONTRACT_RULE_INPUTS =
            "On fungible state(s) deleting, at least one fungible state input must be consumed.";

    final static String CONTRACT_RULE_SUM =
            "On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.isGreaterThan(FungibleUtils.sum(inputs), FungibleUtils.sum(outputs), CONTRACT_RULE_SUM);

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
