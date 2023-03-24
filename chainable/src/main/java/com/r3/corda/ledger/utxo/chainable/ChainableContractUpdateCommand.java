package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Represents the base class for implementing {@link ChainableContract} update commands.
 * This should be implemented by commands intended to update existing ledger instances of {@link ChainableState}.
 */
public abstract class ChainableContractUpdateCommand extends ChainableContractCommand {

    final static String CONTRACT_RULE_INPUTS =
            "On chainable state(s) updating, at least one chainable state must be consumed.";

    final static String CONTRACT_RULE_OUTPUTS =
            "On chainable state(s) updating, at least one chainable state must be created.";

    final static String CONTRACT_RULE_POINTERS =
            "On chainable state(s) updating, the previous state pointer of every created chainable state must not be null.";

    final static String CONTRACT_RULE_EXCLUSIVE_POINTERS =
            "On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively.";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<StateAndRef<ChainableState>> inputs = transaction.getInputStateAndRefs(ChainableState.class);
        final List<ChainableState> outputs = transaction.getOutputStates(ChainableState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() != null, CONTRACT_RULE_POINTERS);

        final Map<StateAndRef<ChainableState>, List<ChainableState>> mappedInputsToOutputs = new HashMap<>();

        for (final StateAndRef<ChainableState> input : inputs) {

            final List<ChainableState> matchingOutputs = new ArrayList<>();

            // TODO : O(n^2) time complexity! :(
            for (final ChainableState output : outputs) {

                if (output.getPreviousStatePointer().isPointingTo(input)) {
                    matchingOutputs.add(output);
                }
            }

            mappedInputsToOutputs.put(input, matchingOutputs);
        }

        Check.all(mappedInputsToOutputs.values(), it -> it.size() == 1, CONTRACT_RULE_EXCLUSIVE_POINTERS);

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
