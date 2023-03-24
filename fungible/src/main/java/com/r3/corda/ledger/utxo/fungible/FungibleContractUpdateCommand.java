package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.crypto.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.math.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

/**
 * Represents the base class for implementing {@link FungibleContract} commands that are intended to update existing ledger instances of
 * {@link FungibleState}.
 * <p>
 * This command will ensure that:
 * <ul>
 *     <li>On fungible state(s) updating, at least one fungible state must be consumed.</li>
 *     <li>On fungible state(s) updating, at least one fungible state must be created.</li>
 *     <li>On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.</li>
 *     <li>On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.</li>
 *     <li>On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states, where the states are grouped by class and identifier hash.</li>
 * </ul>
 */
public abstract class FungibleContractUpdateCommand extends FungibleContractCommand {

    final static String CONTRACT_RULE_INPUTS =
            "On fungible state(s) updating, at least one fungible state must be consumed.";

    final static String CONTRACT_RULE_OUTPUTS =
            "On fungible state(s) updating, at least one fungible state must be created.";

    final static String CONTRACT_RULE_POSITIVE_QUANTITIES =
            "On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.";

    final static String CONTRACT_RULE_SUM =
            "On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.";

    final static String CONTRACT_RULE_GROUP_SUM =
            "On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states, where the states are grouped by class {0} and identifier hash {1}";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_OUTPUTS);
        Check.all(outputs, it -> it.getQuantity().getUnscaledValue().compareTo(BigInteger.ZERO) > 0, CONTRACT_RULE_POSITIVE_QUANTITIES);
        Check.isEqual(FungibleUtils.sum(inputs), FungibleUtils.sum(outputs), CONTRACT_RULE_SUM);

        for (final FungibleState input : inputs) {

            final Class<?> type = input.getClass();
            final SecureHash hash = input.getIdentifierHash();

            final List<FungibleState> inputsByHash = inputs
                    .stream()
                    .filter(it -> it.getClass().equals(type) && it.getIdentifierHash().equals(hash))
                    .collect(Collectors.toList());


            final List<FungibleState> outputsByHash = outputs
                    .stream()
                    .filter(it -> it.getClass().equals(input.getClass()) && it.getIdentifierHash().equals(input.getIdentifierHash()))
                    .collect(Collectors.toList());

            Check.isEqual(FungibleUtils.sum(inputsByHash), FungibleUtils.sum(outputsByHash), MessageFormat.format(CONTRACT_RULE_GROUP_SUM, type.getName(), hash));
        }

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
