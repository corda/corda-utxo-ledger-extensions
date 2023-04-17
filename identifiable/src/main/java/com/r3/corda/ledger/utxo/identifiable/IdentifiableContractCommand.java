package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.VerifiableCommand;

/**
 * Represents the base class for implementing {@link IdentifiableContract} commands.
 */
public abstract class IdentifiableContractCommand<T extends IdentifiableState> implements VerifiableCommand {

    /**
     * Gets the {@link IdentifiableState} type associated with the current command.
     *
     * @return Returns the {@link IdentifiableState} type associated with the current command.
     */
    protected abstract Class<T> getContractStateType();

    /**
     * Initializes a new instance of the {@link IdentifiableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    IdentifiableContractCommand() {
    }
}
