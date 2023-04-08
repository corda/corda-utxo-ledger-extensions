package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.base.TypeUtils;
import com.r3.corda.ledger.utxo.base.VerifiableCommand;

/**
 * Represents the base class for implementing {@link FungibleContract} commands.
 * <p>
 * Subtypes of {@link FungibleContractCommand} cannot be created directly from user code. This is deliberate so that implementations of
 * {@link FungibleContractCommand} cannot circumvent the underlying rules required when creating, updating or deleting (consuming)
 * {@link FungibleState} instances. Instead, user code must derive from one of the following {@link FungibleContractCommand} subtypes:
 * <ul>
 *     <li>{@link FungibleContractCreateCommand} when creating {@link FungibleState} instances.</li>
 *     <li>{@link FungibleContractUpdateCommand} when updating {@link FungibleState} instances.</li>
 *     <li>{@link FungibleContractDeleteCommand} when deleting (consuming) {@link FungibleState} instances.</li>
 * </ul>
 */
public abstract class FungibleContractCommand<T extends FungibleState<?>> implements VerifiableCommand {

    /**
     * Gets the {@link FungibleState} type associated with the current command.
     *
     * @return Returns the {@link FungibleState} type associated with the current command.
     */
    protected Class<T> getContractStateType() {
        return TypeUtils.getGenericArgumentType(getClass());
    }

    /**
     * Initializes a new instance of the {@link FungibleContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    FungibleContractCommand() {
    }
}
