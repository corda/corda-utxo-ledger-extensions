package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.common.*;

/**
 * Represents the base class for implementing {@link IdentifiableContract} commands.
 */
public abstract class IdentifiableContractCommand implements VerifiableCommand {

    /**
     * Initializes a new instance of the {@link IdentifiableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    IdentifiableContractCommand() {
    }
}
