package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.TypeUtils;
import com.r3.corda.ledger.utxo.base.VerifiableCommand;

/**
 * Represents the base class for implementing {@link ChainableContract} commands.
 */
public abstract class ChainableContractCommand<T extends ChainableState<?>> implements VerifiableCommand {

    /**
     * Gets the {@link ChainableState} type associated with the current command.
     *
     * @return Returns the {@link ChainableState} type associated with the current command.
     */
    protected Class<T> getContractStateType() {
        return TypeUtils.getGenericArgumentType(getClass());
    }

    /**
     * Initializes a new instance of the {@link ChainableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    ChainableContractCommand() {
    }
}
