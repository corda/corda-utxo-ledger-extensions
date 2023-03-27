package com.r3.corda.ledger.utxo.chainable;

import com.r3.corda.ledger.utxo.base.DelegatedContract;

/**
 * Represents the contract that governs {@link ChainableState} instances.
 */
public abstract class ChainableContract extends DelegatedContract<ChainableContractCommand> {
}
