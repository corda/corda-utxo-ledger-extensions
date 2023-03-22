package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.common.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.math.*;
import java.util.*;

/**
 * Represents the base class for implementing {@link FungibleContract} create commands.
 * This should be implemented by commands intended to create new ledger instances of {@link FungibleState}.
 */
public abstract class FungibleContractCreateCommand extends FungibleContractCommand {

    final static String CONTRACT_RULE_INPUTS =
            "On fungible state(s) creating, zero fungible state inputs must be consumed.";

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
    protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    }
}
