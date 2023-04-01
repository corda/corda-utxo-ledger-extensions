package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents verification constraints for creating, updating and deleting {@link IdentifiableState} ledger states.
 */
public final class IdentifiableConstraints {

    final static String CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY =
            "On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.";

    /**
     * Prevents instances of {@link IdentifiableConstraints} from being created.
     */
    private IdentifiableConstraints() {
    }

    /**
     * Verifies the {@link IdentifiableContract} create constraints.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        verify(transaction);
    }

    /**
     * Verifies the {@link IdentifiableContract} update constraints.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        verify(transaction);
    }

    /**
     * Verifies the {@link IdentifiableState} delete constraints.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        verify(transaction);
    }

    /**
     * Verifies the {@link IdentifiableContract} delete constraints.
     * This should be implemented by commands intended to delete existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) updating, each identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    private static void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<StateAndRef<IdentifiableState>> inputs = transaction.getInputStateAndRefs(IdentifiableState.class);
        final List<IdentifiableState> outputs = transaction.getOutputStates(IdentifiableState.class);

        final List<StateRef> outputIds = getNonNullOutputIdentifiers(outputs);

        final Map<StateRef, List<StateAndRef<IdentifiableState>>> mappedOutputIdsToInputs =
                mapOutputIdentifiersToInputs(outputIds, inputs);

        Check.all(mappedOutputIdsToInputs.values(), it -> it.size() == 1, CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY);
    }

    /**
     * Gets a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers.
     *
     * @param outputs The output {@link IdentifiableState} states from which to obtain non-null identifiers.
     * @return Returns a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers.
     */
    @NotNull
    private static List<StateRef> getNonNullOutputIdentifiers(@NotNull final List<IdentifiableState> outputs) {
        return outputs
                .stream()
                .map(IdentifiableState::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets a map of non-null identifiers, to input states with a matching identifier.
     * Exactly one state should be mapped for every non-null identifier key.
     *
     * @param outputIdentifiers The identifiers from which to construct map keys.
     * @param inputs            The input states to map to the corresponding identifier.
     * @return Returns a map of non-null identifiers, to input states with a matching identifier.
     */
    @NotNull
    private static Map<StateRef, List<StateAndRef<IdentifiableState>>> mapOutputIdentifiersToInputs(
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
