package com.r3.corda.ledger.utxo.base;

import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Specifies the position of a state within a {@link UtxoLedgerTransaction}.
 */
@CordaSerializable
public enum StatePosition {

    /**
     * Specifies {@link UtxoLedgerTransaction} input states.
     */
    INPUT,

    /**
     * Specifies {@link UtxoLedgerTransaction} output states.
     */
    OUTPUT,

    /**
     * Specifies {@link UtxoLedgerTransaction} reference states.
     */
    REFERENCE;

    /**
     * Obtains a {@link List} of {@link StateAndRef} of type {@link T} from the specified {@link UtxoLedgerTransaction}.
     *
     * @param transaction The {@link UtxoLedgerTransaction} from which to obtain {@link StateAndRef} instances of type {@link T}.
     * @param type        The {@link ContractState} type to obtain from the transaction.
     * @param <T>         The underlying {@link ContractState} type to obtain from the transaction.
     * @return Returns a {@link List} of {@link StateAndRef} of type {@link T} from the specified {@link UtxoLedgerTransaction}.
     */
    @NotNull
    public <T extends ContractState> List<StateAndRef<T>> getStateAndRefs(
            @NotNull final UtxoLedgerTransaction transaction,
            @NotNull final Class<T> type) {
        switch (this) {
            case INPUT:
                return transaction.getInputStateAndRefs(type);
            case OUTPUT:
                return transaction.getOutputStateAndRefs(type);
            case REFERENCE:
                return transaction.getReferenceStateAndRefs(type);
        }

        throw new IllegalArgumentException("The specified StatePosition value is invalid: " + this.name());
    }

    /**
     * Obtains a {@link List} of type {@link T} from the specified {@link UtxoLedgerTransaction}.
     *
     * @param transaction The {@link UtxoLedgerTransaction} from which to obtain instances of type {@link T}.
     * @param type        The {@link ContractState} type to obtain from the transaction.
     * @param <T>         The underlying {@link ContractState} type to obtain from the transaction.
     * @return Returns a {@link List} of type {@link T} from the specified {@link UtxoLedgerTransaction}.
     */
    @NotNull
    public <T extends ContractState> List<T> getStates(
            @NotNull final UtxoLedgerTransaction transaction,
            @NotNull final Class<T> type) {
        switch (this) {
            case INPUT:
                transaction.getInputStates(type);
            case OUTPUT:
                transaction.getOutputStates(type);
            case REFERENCE:
                transaction.getReferenceStates(type);
        }

        throw new IllegalArgumentException("The specified StatePosition value is invalid: " + this.name());
    }
}
