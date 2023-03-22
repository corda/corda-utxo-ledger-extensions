package com.r3.corda.ledger.utxo.common;

import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.util.stream.*;

/**
 * Represents the base class for implementing delegated contracts.
 * A delegated contract facilitates delegation of verification to {@link VerifiableCommand} instances,
 * and visibility checking to {@link VisibleState} instances.
 *
 * @param <T> The underlying {@link VerifiableCommand} type that the contract is responsible for verifying.
 */
public abstract class DelegatedContract<T extends VerifiableCommand> implements Contract {

    /**
     * Gets the permitted {@link VerifiableCommand} types that the current {@link DelegatedContract} is able to verify.
     *
     * @return the permitted {@link VerifiableCommand} types that the current {@link DelegatedContract} is able to verify.
     */
    @NotNull
    protected abstract List<Class<? extends T>> getPermittedCommandTypes();

    /**
     * TODO : Update to isVisible!
     * Determines whether a given state is relevant to a node, given the node's public keys.
     * <p>
     * The default implementation determines that a state is visible to its participants,
     * or if the state implements {@link VisibleState}, then visibility checking can be delegated to the state itself.
     */
    @Override
    public boolean isRelevant(@NotNull final ContractState state, @NotNull final Set<PublicKey> myKeys) {
        return Contract.super.isRelevant(state, myKeys) || state instanceof VisibleState && ((VisibleState) state).isVisible(myKeys);
    }

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    @Override
    public final void verify(@NotNull final UtxoLedgerTransaction transaction) {
        onVerify(transaction);

        boolean atLeastOneCommandFoundToExecute = false;

        for (final VerifiableCommand command : transaction.getCommands(VerifiableCommand.class)) {
            if (getPermittedCommandTypes().contains(command.getClass())) {
                atLeastOneCommandFoundToExecute = true;
                command.verify(transaction);
            }
        }


        Check.isTrue(atLeastOneCommandFoundToExecute, "At least one command of type '"
                + getGenericTypeParameterName(getClass())
                + "' must be included in the transaction.\n"
                + "The permitted commands include ["
                + getPermittedCommandTypes().stream().map(Class::getSimpleName).collect(Collectors.joining(", "))
                + "]");
    }

    /**
     * Verifies the specified transaction associated with the current contract.
     *
     * @param transaction The transaction to verify.
     * @throws RuntimeException if the specified transaction fails verification.
     */
    protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    }

    private String getGenericTypeParameterName(Class<?> type) {
        Type superClass = type.getGenericSuperclass();

        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            return parameterizedType.getActualTypeArguments()[0].getTypeName();
        }

        return getGenericTypeParameterName((Class<?>) superClass);
    }
}
