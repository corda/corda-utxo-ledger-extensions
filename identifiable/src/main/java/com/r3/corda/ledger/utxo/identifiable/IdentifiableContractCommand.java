package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.ContractStateType;
import com.r3.corda.ledger.utxo.base.VerifiableCommand;

/**
 * Represents the base class for implementing {@link IdentifiableContract} commands.
 */
public abstract class IdentifiableContractCommand<T extends IdentifiableState> implements VerifiableCommand, ContractStateType<T> {

    /**
     * Initializes a new instance of the {@link IdentifiableContractCommand} class.
     * This constructor is intentionally package-private (internal) to control the allowable subtypes.
     */
    IdentifiableContractCommand() {
    }
}
