package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Represents the base class for implementing {@link FungibleContract} create commands.
 * This should be implemented by commands intended to delete existing ledger instances of {@link FungibleState}.
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
        Check.isGreaterThan(FungibleState.sum(inputs), FungibleState.sum(outputs), CONTRACT_RULE_SUM);

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
