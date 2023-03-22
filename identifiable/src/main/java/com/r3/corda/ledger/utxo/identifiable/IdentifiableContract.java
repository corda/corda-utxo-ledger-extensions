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

        final List<StateRef> outputIds = getNonNullOutputIdentifiers(outputs);

        final Map<StateRef, List<StateAndRef<IdentifiableState>>> mappedOutputIdsToInputs =
                mapOutputIdentifiersToInputs(outputIds, inputs);

        Check.all(mappedOutputIdsToInputs.values(), it -> it.size() == 1, CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY);
    }

    @NotNull
    private List<StateRef> getNonNullOutputIdentifiers(@NotNull final List<IdentifiableState> outputs) {
        return outputs
                .stream()
                .map(IdentifiableState::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    private Map<StateRef, List<StateAndRef<IdentifiableState>>> mapOutputIdentifiersToInputs(
            @NotNull final List<StateRef> outputIdentifiers,
            @NotNull final List<StateAndRef<IdentifiableState>> inputs) {
        final Map<StateRef, List<StateAndRef<IdentifiableState>>> result = new HashMap<>();

        for (final StateRef outputIdentifier : outputIdentifiers) {
            final List<StateAndRef<IdentifiableState>> inputsMatchingOutputIdentifier = new ArrayList<>();

            if (!result.containsKey(outputIdentifier)) {
                result.put(outputIdentifier, inputsMatchingOutputIdentifier);
            }

            for (final StateAndRef<IdentifiableState> input : inputs) {
                final StateRef inputRef = input.getRef();
                final StateRef inputIdentifier = input.getState().getContractState().getId();

                if (outputIdentifier.equals(inputRef) || outputIdentifier.equals(inputIdentifier)) {
                    result.get(outputIdentifier).add(input);
                }
            }
        }

        return result;
    }
}
