package com.r3.corda.ledger.utxo.base;

import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.ContractState;
import org.jetbrains.annotations.NotNull;

/**
 * Defines a mechanism to obtain a {@link ContractState} type associated with the current {@link Command}.
 *
 * @param <T> The underlying {@link ContractState} type associated with the current {@link Command}.
 */
public interface ContractStateType<T extends ContractState> extends Command {

    /**
     * Gets the {@link ContractState} type associated with the current command.
     *
     * @return Returns the {@link ContractState} type associated with the current command.
     */
    @NotNull
    Class<T> getContractStateType();
}
