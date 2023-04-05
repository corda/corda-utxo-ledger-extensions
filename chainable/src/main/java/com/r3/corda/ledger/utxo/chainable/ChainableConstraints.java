package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.Check;
import com.r3.corda.ledger.utxo.base.StaticPointer;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents verification constraints for creating, updating and deleting {@link ChainableState} instances.
 */
public final class ChainableConstraints {

    final static String CONTRACT_RULE_CREATE_OUTPUTS =
            "On chainable state(s) creating, at least one chainable state must be created.";

    final static String CONTRACT_RULE_CREATE_POINTERS =
            "On chainable state(s) creating, the previous state pointer of every created chainable state must be null.";

    final static String CONTRACT_RULE_UPDATE_INPUTS =
            "On chainable state(s) updating, at least one chainable state must be consumed.";

    final static String CONTRACT_RULE_UPDATE_OUTPUTS =
            "On chainable state(s) updating, at least one chainable state must be created.";

    final static String CONTRACT_RULE_UPDATE_POINTERS =
            "On chainable state(s) updating, the previous state pointer of every created chainable state must not be null.";

    final static String CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS =
            "On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively.";

    final static String CONTRACT_RULE_DELETE_INPUTS =
            "On chainable state(s) deleting, at least one chainable state must be consumed.";

    private final static int OUTPUTS_PER_INPUT = 1;

    /**
     * Prevents instances of {@link ChainableConstraints} from being created.
     */
    private ChainableConstraints() {
    }

    /**
     * Verifies the {@link ChainableContract} create constraints.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link ChainableState} and will verify the following constraints:
     * <ol>
     *     <li>On chainable state(s) creating, at least one chainable state must be created.</li>
     *     <li>On chainable state(s) creating, the previous state pointer of every created chainable state must be null.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("rawtypes")
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        final List<ChainableState> outputs = transaction.getOutputStates(ChainableState.class);

        Check.isNotEmpty(outputs, CONTRACT_RULE_CREATE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() == null, CONTRACT_RULE_CREATE_POINTERS);
    }

    /**
     * Verifies the {@link ChainableContract} update constraints.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link ChainableState} and will verify the following constraints:
     * <ol>
     *  <li>On chainable state(s) updating, at least one chainable state must be consumed.</li>
     *  <li>On chainable state(s) updating, at least one chainable state must be created.</li>
     *  <li>On chainable state(s) updating, the previous state pointer of every created chainable state must not be null.</li>
     *  <li>On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("rawtypes")
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        final List<StateAndRef<ChainableState>> inputs = transaction.getInputStateAndRefs(ChainableState.class);
        final List<ChainableState> outputs = transaction.getOutputStates(ChainableState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_UPDATE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_UPDATE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() != null, CONTRACT_RULE_UPDATE_POINTERS);

        final Map<StaticPointer<ChainableState>, List<StaticPointer<ChainableState>>> mappedInputsToOutputs =
                mapInputsToOutputs(inputs, outputs);

        Check.all(mappedInputsToOutputs.values(), it -> it.size() == OUTPUTS_PER_INPUT, CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS);
    }

    /**
     * Verifies the {@link ChainableContract} delete constraints.
     * This should be implemented by commands intended to delete existing ledger instances of {@link ChainableState} and will verify the following constraints:
     * <ol>
     *     <li>On chainable state(s) deleting, at least one chainable state must be consumed.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("rawtypes")
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        final List<ChainableState> inputs = transaction.getInputStates(ChainableState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_DELETE_INPUTS);
    }

    /**
     * Gets a {@link Map} of synthetic input {@link StaticPointer} of type {@link ChainableState} to output states with a matching {@link StaticPointer}.
     * The input pointers are considered synthetic because they are generated from the inputs themselves; i.e. they are not real pointers.
     *
     * @param inputs  The inputs from which to construct synthetic pointers.
     * @param outputs The outputs to map to each synthetic pointer.
     * @return Returns a {@link Map} of synthetic input {@link StaticPointer} of type {@link ChainableState} to output states with a matching {@link StaticPointer}.
     */
    @NotNull
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<StaticPointer<ChainableState>, List<StaticPointer<ChainableState>>> mapInputsToOutputs(
            @NotNull final List<StateAndRef<ChainableState>> inputs,
            @NotNull final List<ChainableState> outputs) {
        final Map<StaticPointer<ChainableState>, List<StaticPointer<ChainableState>>> result = new HashMap<>();

        for (StateAndRef<ChainableState> input : inputs) {
            final StaticPointer<ChainableState> inputPointer = (StaticPointer<ChainableState>) new StaticPointer<>(
                    input.getRef(),
                    input.getState().getContractState().getClass()
            );

            result.put(inputPointer, new ArrayList<>());
        }

        for (ChainableState output : outputs) {
            final StaticPointer<ChainableState> outputPointer = output.getPreviousStatePointer();
            if (result.containsKey(outputPointer)) {
                result.get(outputPointer).add(outputPointer);
            }
        }

        return result;
    }
}
