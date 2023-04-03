package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents verification constraints for creating, updating and deleting {@link FungibleState} instances.
 */
public final class FungibleConstraints {

    final static String CONTRACT_RULE_CREATE_INPUTS =
            "On fungible state(s) creating, zero fungible states must be consumed.";

    final static String CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES =
            "On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.";

    final static String CONTRACT_RULE_UPDATE_INPUTS =
            "On fungible state(s) updating, at least one fungible state must be consumed.";

    final static String CONTRACT_RULE_UPDATE_OUTPUTS =
            "On fungible state(s) updating, at least one fungible state must be created.";

    final static String CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES =
            "On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.";

    final static String CONTRACT_RULE_UPDATE_SUM =
            "On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.";

    final static String CONTRACT_RULE_UPDATE_GROUP_SUM =
            "On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.";

    final static String CONTRACT_RULE_DELETE_INPUTS =
            "On fungible state(s) deleting, at least one fungible state input must be consumed.";

    final static String CONTRACT_RULE_DELETE_SUM =
            "On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.";

    /**
     * Prevents instances of {@link FungibleConstraints} from being created.
     */
    private FungibleConstraints() {
    }

    /**
     * Verifies the {@link FungibleContract} create constraints.
     * <p>
     * This should be implemented by commands intended to create new ledger instances of {@link FungibleState} and will verify the following constraints:
     * <ol>
     *     <li>On fungible state(s) creating, zero fungible states must be consumed.</li>
     *     <li>On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("rawtypes")
    public static void verifyCreate(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);
        // TODO : CORE-12120 - Review missing contract rules (check outputs)

        Check.isEmpty(inputs, CONTRACT_RULE_CREATE_INPUTS);
        Check.all(outputs, it -> it.getQuantity().getUnscaledValue().compareTo(BigInteger.ZERO) > 0, CONTRACT_RULE_CREATE_POSITIVE_QUANTITIES);
    }

    /**
     * Verifies the {@link FungibleContract} update constraints.
     * <p>
     * This should be implemented by commands intended to update existing ledger instances of {@link FungibleState} and will verify the following constraints:
     * <ol>
     *  <li>On fungible state(s) updating, at least one fungible state must be consumed.</li>
     *  <li>On fungible state(s) updating, at least one fungible state must be created.</li>
     *  <li>On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.</li>
     *  <li>On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.</li>
     *  <li>On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void verifyUpdate(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_UPDATE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_UPDATE_OUTPUTS);
        Check.all(outputs, it -> it.getQuantity().getUnscaledValue().compareTo(BigInteger.ZERO) > 0, CONTRACT_RULE_UPDATE_POSITIVE_QUANTITIES);
        Check.isEqual(FungibleUtils.sum(inputs), FungibleUtils.sum(outputs), CONTRACT_RULE_UPDATE_SUM);

        for (final FungibleState input : inputs) {

            // 1. We need to obtain all the inputs that are fungible with the current input.
            final List<FungibleState> fungibleInputs = inputs
                    .stream()
                    .filter(it -> input.isFungibleWith(it))
                    .collect(Collectors.toList());

            // 2. We need to obtain all the outputs that are fungible with the current input.
            final List<FungibleState> fungibleOutputs = outputs
                    .stream()
                    .filter(it -> input.isFungibleWith(it))
                    .collect(Collectors.toList());

            // 3. Generate the sum of all fungible inputs and outputs.
            final BigInteger inputSum = FungibleUtils.sum(fungibleInputs);
            final BigInteger outputSum = FungibleUtils.sum(fungibleOutputs);

            Check.isEqual(inputSum, outputSum, CONTRACT_RULE_UPDATE_GROUP_SUM);
        }
    }

    /**
     * Verifies the {@link FungibleContract} delete constraints.
     * This should be implemented by commands intended to delete existing ledger instances of {@link FungibleState} and will verify the following constraints:
     * <ol>
     *     <li>On fungible state(s) deleting, at least one fungible state input must be consumed.</li>
     *     <li>On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.</li>
     * </ol>
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @SuppressWarnings("rawtypes")
    public static void verifyDelete(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_DELETE_INPUTS);
        Check.isGreaterThan(FungibleUtils.sum(inputs), FungibleUtils.sum(outputs), CONTRACT_RULE_DELETE_SUM);
    }
}
