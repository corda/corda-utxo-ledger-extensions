package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TransactionState;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents verification constraints for creating, updating and deleting {@link IdentifiableState} ledger states.
 */
public final class IdentifiableConstraints {

    final static String CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY =
            "On identifiable state(s) updating, each created identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.";

    private final static int MAX_OUTPUTS_PER_INPUT = 1;

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

        final List<StateRef> inputIds = getInputIdentifiers(inputs);
        final List<StateRef> outputIds = getOutputIdentifiers(outputs);
        final Map<StateRef, List<StateRef>> mappedInputsToOutputs = mapInputsToOutputs(inputIds, outputIds);

        Check.all(mappedInputsToOutputs.values(), it -> it.size() <= MAX_OUTPUTS_PER_INPUT, CONTRACT_RULE_IDENTIFIER_EXCLUSIVITY);
    }

    /**
     * Gets a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers from the specified transaction outputs.
     *
     * @param outputs The output {@link IdentifiableState} states from which to obtain non-null identifiers.
     * @return Returns a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers.
     */
    @NotNull
    private static List<StateRef> getOutputIdentifiers(@NotNull final List<IdentifiableState> outputs) {
        return outputs
                .stream()
                .map(IdentifiableState::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers from the specified transaction inputs.
     *
     * @param inputs The input {@link StateAndRef} of  {@link IdentifiableState} states from which to obtain non-null identifiers.
     * @return Returns a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers.
     */
    private static List<StateRef> getInputIdentifiers(@NotNull final List<StateAndRef<IdentifiableState>> inputs) {
        final Stream<StateRef> inputRefs = inputs
                .stream()
                .map(StateAndRef::getRef);

        final Stream<StateRef> inputIdentifiers = inputs
                .stream()
                .map(StateAndRef::getState)
                .map(TransactionState::getContractState)
                .map(IdentifiableState::getId)
                .filter(Objects::nonNull);

        return Stream
                .concat(inputRefs, inputIdentifiers)
                .collect(Collectors.toList());
    }

    /**
     * Gets a map of non-null identifiers, to input states with a matching identifier.
     * Exactly one state should be mapped for every non-null identifier key.
     *
     * @param inputIds  The identifiers from which to construct map keys.
     * @param outputIds The identifiers for which zero, one, or many may map to each key.
     * @return Returns a map of input identifiers which are mapped to zero, one or many output identifiers.
     */
    @NotNull
    private static Map<StateRef, List<StateRef>> mapInputsToOutputs(
            @NotNull final List<StateRef> inputIds,
            @NotNull final List<StateRef> outputIds) {
        final Map<StateRef, List<StateRef>> result = new HashMap<>();

        for (final StateRef inputId : inputIds) {
            result.put(inputId, new ArrayList<>());
        }

        for (final StateRef outputId : outputIds) {
            if (result.containsKey(outputId)) {
                result.get(outputId).add(outputId);
            }
        }

        return result;
    }
}
