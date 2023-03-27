package com.r3.corda.ledger.utxo.base;

import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.Set;

/**
 * Defines a mechanism to delegate visibility checking rules to contract states.
 */
public interface VisibleState extends ContractState {

    /**
     * Determines whether the current state is visible to a node observing, or recording the associated transaction.
     * <p>
     * The default implementation is that contract states are only visible to their participants.
     * </p>
     *
     * @param checker Provides a mechanism to determine visibility of the specified {@link ContractState}.
     *
     * @return Returns true if the specified state is visible to the current node; otherwise, false.
     */
    @Suspendable
    default boolean isVisible(@NotNull VisibilityChecker checker) {
        return checker.containsMySigningKeys(getParticipants());
    }
}
