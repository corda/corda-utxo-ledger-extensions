package com.r3.corda.ledger.utxo.base;

import net.corda.v5.ledger.utxo.Contract;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.VisibilityChecker;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the base class for implementing delegated contracts.
 * A delegated contract facilitates delegation of verification to {@link VerifiableCommand} instances,
 * and visibility checking to {@link VisibleState} instances.
 *
 * @param <T> The underlying {@link VerifiableCommand} type that the contract is responsible for verifying.
 */
public abstract class DelegatedContract<T extends VerifiableCommand> implements Contract {

    static String CONTRACT_RULE_EMPTY_PERMITTED_COMMAND_TYPES =
            "On ''{0}'' contract executing, at least one permitted command type must be specified.";

    static String CONTRACT_RULE_EXECUTE_PERMITTED_COMMANDS =
            "On ''{0}'' contract executing, at least one command of type ''{1}'' must be included in the transaction.\nThe permitted commands include [{2}].";

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
    public boolean isVisible(@NotNull final ContractState state, @NotNull VisibilityChecker checker) {
        return Contract.super.isVisible(state, checker) || state instanceof VisibleState && ((VisibleState) state).isVisible(checker);
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

        final List<Class<? extends T>> permittedCommandTypes = getPermittedCommandTypes();

        Check.isNotEmpty(permittedCommandTypes, MessageFormat.format(
                CONTRACT_RULE_EMPTY_PERMITTED_COMMAND_TYPES,
                getClass().getTypeName()
        ));

        boolean hasExecutedAtLeastOnePermittedCommand = false;

        for (final VerifiableCommand command : transaction.getCommands(VerifiableCommand.class)) {
            if (permittedCommandTypes.contains(command.getClass())) {
                hasExecutedAtLeastOnePermittedCommand = true;
                command.verify(transaction);
            }
        }

        Check.isTrue(hasExecutedAtLeastOnePermittedCommand, MessageFormat.format(
                CONTRACT_RULE_EXECUTE_PERMITTED_COMMANDS,
                getClass().getTypeName(),
                getGenericTypeParameterName(getClass()),
                getPermittedCommandTypes().stream().map(Class::getSimpleName).collect(Collectors.joining(", "))
        ));
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

    /**
     * Obtains the generic parameter type of type {@link T} by recursively looking up the type hierarchy.
     *
     * @param type The {@link Class} to begin looking for the generic parameter type of type {@link T}.
     * @return Returns the generic parameter type of type {@link T} by recursively looking up the type hierarchy.
     */
    private String getGenericTypeParameterName(Class<?> type) {
        Type superClass = type.getGenericSuperclass();

        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            return parameterizedType.getActualTypeArguments()[0].getTypeName();
        }

        return getGenericTypeParameterName((Class<?>) superClass);
    }
}
