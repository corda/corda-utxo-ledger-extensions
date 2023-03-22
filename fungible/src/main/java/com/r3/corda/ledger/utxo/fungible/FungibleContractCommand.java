package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.common.*;

/**
 * Represents the base class for implementing {@link FungibleContract} commands.
 */
public abstract class FungibleContractCommand implements VerifiableCommand {

    /**
     * Initializes a new instance of the {@link FungibleContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    FungibleContractCommand() {
    }
}
