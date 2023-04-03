package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.DelegatedContract;

/**
 * Represents the contract that governs {@link IdentifiableState} instances.
 */
public abstract class IdentifiableContract extends DelegatedContract<IdentifiableContractCommand> {
}
