package com.r3.corda.ledger.utxo.fungible;

import com.r3.corda.ledger.utxo.common.*;

/**
 * Represents the contract that governs {@link FungibleState} instances.
 */
public abstract class FungibleContract extends DelegatedContract<FungibleContractCommand> {
}
