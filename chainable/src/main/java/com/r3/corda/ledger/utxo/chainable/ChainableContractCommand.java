package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.ContractStateType;
import com.r3.corda.ledger.utxo.base.VerifiableCommand;

/**
 * Represents the base class for implementing {@link ChainableContract} commands.
 */
public abstract class ChainableContractCommand<T extends ChainableState<?>> implements VerifiableCommand, ContractStateType<T> {

    /**
     * Initializes a new instance of the {@link ChainableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    ChainableContractCommand() {
    }
}
