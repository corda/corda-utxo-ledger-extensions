package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

/**
 * Represents the contract that governs {@link IdentifiableState} instances.
 */
public abstract class IdentifiableContract extends DelegatedContract<IdentifiableContractCommand> {

    final static String CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY =
            "On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    protected final void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<StateAndRef<IdentifiableState>> inputs = transaction.getInputStateAndRefs(IdentifiableState.class);
        final List<IdentifiableState> outputs = transaction.getOutputStates(IdentifiableState.class);

        final List<StateRef> stateRefs = outputs
                .stream()
                .map(IdentifiableState::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        /*
         * Obtains a map of all the non-null output IDs, to a list of [StateAndRef] of [IdentifiableState] where:
         * 1. The ID is the [StateRef] of the input (an n == 1 length state chain is evolving in this transaction).
         * 2. The ID is the ID of the input (an n > 1 length state chain is evolving in this transaction).
         */
        final Map<StateRef, List<StateAndRef<IdentifiableState>>> mappedOutputIdsToInputs = new HashMap<>();

        for (final StateRef stateRef : stateRefs) {

            final List<StateAndRef<IdentifiableState>> inputsMatchingStateRef = new ArrayList<>();

            // TODO : O(n^2) time complexity! :(
            for (final StateAndRef<IdentifiableState> input : inputs) {
                if (input.getRef() == stateRef || input.getState().getContractState().getId() == stateRef) {
                    inputsMatchingStateRef.add(input);
                }
            }

            mappedOutputIdsToInputs.put(stateRef, inputsMatchingStateRef);
        }

        /*
         * Each entry in the map must contain only one output to prevent splitting or merging identifiable states.
         */

        assert mappedOutputIdsToInputs.values().stream().allMatch(it -> it.size() == 1) : CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY;
    }
}
