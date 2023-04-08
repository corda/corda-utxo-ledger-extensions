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
     * @param type        The {@link ChainableState} type to verify.
     * @param <T>         The underlying {@link ChainableState} type to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends ChainableState<?>> void verifyCreate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<T> outputs = transaction.getOutputStates(type);

        Check.isNotEmpty(outputs, CONTRACT_RULE_CREATE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() == null, CONTRACT_RULE_CREATE_POINTERS);
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
    @SuppressWarnings("unused")
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyCreate(transaction, ChainableState.class);
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
     * @param type        The {@link ChainableState} type to verify.
     * @param <T>         The underlying {@link ChainableState} type to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends ChainableState<?>> void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<StateAndRef<T>> inputs = transaction.getInputStateAndRefs(type);
        final List<T> outputs = transaction.getOutputStates(type);

        Check.isNotEmpty(inputs, CONTRACT_RULE_UPDATE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_UPDATE_OUTPUTS);
        Check.all(outputs, it -> it.getPreviousStatePointer() != null, CONTRACT_RULE_UPDATE_POINTERS);

        final Map<StaticPointer<T>, List<StaticPointer<T>>> mappedInputsToOutputs = mapInputsToOutputs(inputs, outputs);

        Check.all(mappedInputsToOutputs.values(), it -> it.size() == OUTPUTS_PER_INPUT, CONTRACT_RULE_UPDATE_EXCLUSIVE_POINTERS);
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
    @SuppressWarnings("unused")
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyUpdate(transaction, ChainableState.class);
    }

    /**
     * Verifies the {@link ChainableContract} delete constraints.
     * This should be implemented by commands intended to delete existing ledger instances of {@link ChainableState} and will verify the following constraints:
     * <ol>
     *     <li>On chainable state(s) deleting, at least one chainable state must be consumed.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The {@link ChainableState} type to verify.
     * @param <T>         The underlying {@link ChainableState} type to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    public static <T extends ChainableState<?>> void verifyDelete(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<T> inputs = transaction.getInputStates(type);

        Check.isNotEmpty(inputs, CONTRACT_RULE_DELETE_INPUTS);
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
    @SuppressWarnings("unused")
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        verifyDelete(transaction, ChainableState.class);
    }

    /**
     * Gets a {@link Map} of synthetic input {@link StaticPointer} of type {@link ChainableState} to output states with a matching {@link StaticPointer}.
     * The input pointers are considered synthetic because they are generated from the inputs themselves; i.e. they are not real pointers.
     *
     * @param inputs  The inputs from which to construct synthetic pointers.
     * @param outputs The outputs to map to each synthetic pointer.
     * @param <T>     The underlying {@link ChainableState} type.
     * @return Returns a {@link Map} of synthetic input {@link StaticPointer} of type {@link ChainableState} to output states with a matching {@link StaticPointer}.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    private static <T extends ChainableState<?>> Map<StaticPointer<T>, List<StaticPointer<T>>> mapInputsToOutputs(
            @NotNull final List<StateAndRef<T>> inputs,
            @NotNull final List<T> outputs) {

        final Map<StaticPointer<T>, List<StaticPointer<T>>> result = new HashMap<>();

        for (StateAndRef<T> input : inputs) {
            final Class<T> type = (Class<T>) input.getState().getContractState().getClass();
            final StaticPointer<T> inputPointer = new StaticPointer<>(input.getRef(), type);

            result.put(inputPointer, new ArrayList<>());
        }

        for (T output : outputs) {
            final StaticPointer<T> outputPointer = (StaticPointer<T>) output.getPreviousStatePointer();
            if (result.containsKey(outputPointer)) {
                result.get(outputPointer).add(outputPointer);
            }
        }

        return result;
    }
}
