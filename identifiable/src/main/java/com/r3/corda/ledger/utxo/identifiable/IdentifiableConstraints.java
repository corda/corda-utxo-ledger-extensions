package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.TransactionState;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents verification constraints for creating, updating and deleting {@link IdentifiableState} ledger states.
 */
public final class IdentifiableConstraints {

    final static String CONTRACT_RULE_CREATE_OUTPUTS =
            "On identifiable state(s) creating, at least one identifiable state must be created.";

    final static String CONTRACT_RULE_UPDATE_INPUTS =
            "On identifiable state(s) updating, at least one identifiable state must be consumed.";

    final static String CONTRACT_RULE_UPDATE_OUTPUTS =
            "On identifiable state(s) updating, at least one identifiable state must be created.";

    final static String CONTRACT_RULE_UPDATE_IDENTIFIERS =
            "On identifiable state(s) updating, only one identifiable state with a matching identifier must be consumed for every created identifiable state with a non-null identifier.";

    final static String CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY =
            "On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null.";

    final static String CONTRACT_RULE_DELETE_INPUTS =
            "On identifiable state(s) deleting, at least one identifiable state must be consumed.";

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
     *     <li>On identifiable state(s) creating, at least one identifiable state must be created.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link IdentifiableState} to verify.
     * @param <T>         The underlying type of {@link IdentifiableState} to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    public static <T extends IdentifiableState> void verifyCreate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<T> outputs = transaction.getOutputStates(type);

        Check.isNotEmpty(outputs, CONTRACT_RULE_CREATE_OUTPUTS);
    }

    /**
     * Verifies the {@link IdentifiableContract} create constraints.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) creating, at least one identifiable state must be created.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyCreate(transaction, IdentifiableState.class);
    }

    /**
     * Verifies the {@link IdentifiableContract} update constraints.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) updating, at least one identifiable state must be consumed.</li>
     *     <li>On identifiable state(s) updating, at least one identifiable state must be created.</li>
     *     <li>On identifiable state(s) updating, only one identifiable state with a matching identifier must be consumed for every created identifiable state with a non-null identifier.</li>
     *     <li>On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link IdentifiableState} to verify.
     * @param <T>         The underlying type of {@link IdentifiableState} to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    public static <T extends IdentifiableState> void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<StateAndRef<T>> inputs = transaction.getInputStateAndRefs(type);
        final List<T> outputs = transaction.getOutputStates(type);

        Check.isNotEmpty(inputs, CONTRACT_RULE_UPDATE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_UPDATE_OUTPUTS);

        final List<StateRef> inputIds = getInputIdentifiers(inputs);
        final List<StateRef> outputIds = getNonNullOutputIdentifiers(outputs);

        Check.all(outputIds, inputIds::contains, CONTRACT_RULE_UPDATE_IDENTIFIERS);
        Check.isDistinct(outputIds, CONTRACT_RULE_UPDATE_IDENTIFIER_EXCLUSIVITY);
    }

    /**
     * Verifies the {@link IdentifiableContract} update constraints.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) updating, at least one identifiable state must be consumed.</li>
     *     <li>On identifiable state(s) updating, at least one identifiable state must be created.</li>
     *     <li>On identifiable state(s) updating, only one identifiable state with a matching identifier must be consumed for every created identifiable state with a non-null identifier.</li>
     *     <li>On identifiable state(s) updating, every created identifiable state's identifier must appear only once when the identifier is not null.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        verifyUpdate(transaction, IdentifiableState.class);
    }

    /**
     * Verifies the {@link IdentifiableState} delete constraints.
     * <p>
     * This should be implemented by commands intended to delete existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) deleting, at least one identifiable state must be consumed.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @param type        The type of {@link IdentifiableState} to verify.
     * @param <T>         The underlying type of {@link IdentifiableState} to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    public static <T extends IdentifiableState> void verifyDelete(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type) {
        final List<T> inputs = transaction.getInputStates(type);

        Check.isNotEmpty(inputs, CONTRACT_RULE_DELETE_INPUTS);
    }

    /**
     * Verifies the {@link IdentifiableState} delete constraints.
     * <p>
     * This should be implemented by commands intended to delete existing ledger instances of {@link IdentifiableState} and will verify the following constraints:
     * <ol>
     *     <li>On identifiable state(s) deleting, at least one identifiable state must be consumed.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws IllegalStateException if the specified transaction fails verification.
     */
    @SuppressWarnings("unused")
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        verifyDelete(transaction, IdentifiableState.class);
    }

    /**
     * Gets a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers from the specified transaction outputs.
     *
     * @param outputs The output {@link IdentifiableState} states from which to obtain non-null identifiers.
     * @return Returns a {@link List} of {@link StateRef} of non-null {@link IdentifiableState} identifiers.
     */
    @NotNull
    private static <T extends IdentifiableState> List<StateRef> getNonNullOutputIdentifiers(@NotNull final List<T> outputs) {
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
    private static <T extends IdentifiableState> List<StateRef> getInputIdentifiers(@NotNull final List<StateAndRef<T>> inputs) {
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
}
