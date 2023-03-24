package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.common.*;

/**
 * Represents the base class for implementing {@link ChainableContract} commands.
 */
public abstract class ChainableContractCommand implements VerifiableCommand {

    /**
     * Initializes a new instance of the {@link ChainableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    ChainableContractCommand() {
    }
}
