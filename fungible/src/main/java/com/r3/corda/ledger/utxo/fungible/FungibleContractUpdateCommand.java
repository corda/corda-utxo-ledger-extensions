package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

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
 *     <li>On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.</li>
 * </ul>
 */
public abstract class FungibleContractUpdateCommand extends FungibleContractCommand {

    final static String CONTRACT_RULE_INPUTS = "On fungible state(s) updating, at least one fungible state must be consumed.";

    final static String CONTRACT_RULE_OUTPUTS = "On fungible state(s) updating, at least one fungible state must be created.";

    final static String CONTRACT_RULE_POSITIVE_QUANTITIES = "On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.";

    final static String CONTRACT_RULE_SUM = "On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.";

    final static String CONTRACT_RULE_GROUP_SUM = "On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.";

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        final List<FungibleState> inputs = transaction.getInputStates(FungibleState.class);
        final List<FungibleState> outputs = transaction.getOutputStates(FungibleState.class);

        Check.isNotEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.isNotEmpty(outputs, CONTRACT_RULE_OUTPUTS);
        Check.all(outputs, it -> it.getQuantity().getUnscaledValue().compareTo(BigInteger.ZERO) > 0, CONTRACT_RULE_POSITIVE_QUANTITIES);
        Check.isEqual(FungibleUtils.sum(inputs), FungibleUtils.sum(outputs), CONTRACT_RULE_SUM);

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

            Check.isEqual(inputSum, outputSum, CONTRACT_RULE_GROUP_SUM);
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
