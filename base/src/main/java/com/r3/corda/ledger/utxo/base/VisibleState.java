package com.r3.corda.ledger.utxo.base;

import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.Set;

/**
 * Defines a mechanism to delegate visibility checking rules to contract states.
 */
public interface VisibleState extends ContractState {

    /**
     * Determines whether the current state is visible to the current node.
     * The default implementation is that contract states are only visible to their participants.
     *
     * @param myKeys My ledger keys.
     * @return Returns true if the current state is visible to the current node; otherwise, false.
     */
    default boolean isVisible(@NotNull final Set<PublicKey> myKeys) {
        return getParticipants().stream().anyMatch(myKeys::contains);
    }
}
