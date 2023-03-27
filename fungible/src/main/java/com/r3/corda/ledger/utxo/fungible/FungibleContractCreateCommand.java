package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.base.Check;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.List;

/**
 * Represents the base class for implementing {@link FungibleContract} commands that are intended to create new ledger instances of
 * {@link FungibleState}.
 * <p>
 * This command will ensure that:
 * <ul>
 *     <li>On fungible state(s) creating, zero fungible states must be consumed.</li>
 *     <li>On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.</li>
 * </ul>
 */
public abstract class FungibleContractCreateCommand extends FungibleContractCommand {

    final static String CONTRACT_RULE_INPUTS =
            "On fungible state(s) creating, zero fungible states must be consumed.";

    final static String CONTRACT_RULE_POSITIVE_QUANTITIES =
            "On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.";

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

        Check.isEmpty(inputs, CONTRACT_RULE_INPUTS);
        Check.all(outputs, it -> it.getQuantity().getUnscaledValue().compareTo(BigInteger.ZERO) > 0, CONTRACT_RULE_POSITIVE_QUANTITIES);

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
