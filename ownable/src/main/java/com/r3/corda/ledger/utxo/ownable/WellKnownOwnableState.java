package com.r3.corda.ledger.utxo.ownable;

import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a mechanism for implementing well-known ownable states.
 */
public interface WellKnownOwnableState extends ContractState {

    /**
     * Gets the issuer's {@link  MemberX500Name} of the current {@link WellKnownOwnableState}.
     *
     * @return Returns the issuer's {@link  MemberX500Name} of the current {@link WellKnownOwnableState}.
     */
    @NotNull
    MemberX500Name getOwnerName();
}
