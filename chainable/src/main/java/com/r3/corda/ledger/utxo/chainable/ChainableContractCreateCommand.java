package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents the base class for implementing {@link ChainableContract} create commands.
 * This should be implemented by commands intended to create new ledger instances of {@link ChainableState}.
 */
public abstract class ChainableContractCreateCommand extends ChainableContractCommand {

    final static String CONTRACT_RULE_INPUTS =
            "On chainable state(s) creating, zero chainable states must be consumed.";

    final static String CONTRACT_RULE_OUTPUTS =
            "On chainable state(s) creating, at least one chainable state must be created.";

    final static String CONTRACT_RULE_POINTERS =
            "On chainable state(s) creating, the previous state pointer of every created chainable state must be null.";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<ChainableState> inputs = transaction.getInputStates(ChainableState.class);
        final List<ChainableState> outputs = transaction.getOutputStates(ChainableState.class);

        Check.isEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() == null, CONTRACT_RULE_POINTERS);

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
