package com.r3.corda.ledger.utxo.identifiable;

import com.r3.corda.ledger.utxo.base.StatePointer;
import com.r3.corda.ledger.utxo.base.StatePosition;
import kotlin.NotImplementedError;
import net.corda.v5.ledger.utxo.ContractState;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * Represents an identifiable pointer, which points to a specific {@link StateRef},
 * or {@link IdentifiableState} whose ID is equal to the specified {@link StateRef} on the ledger.
 *
 * @param <T> The underlying {@link ContractState} type that the current pointer is pointing to.
 */
@SuppressWarnings("unused")
public final class IdentifiablePointer<T extends IdentifiableState> implements StatePointer<T> {

    /**
     * The {@link StateRef} that the current pointer is pointing to.
     */
    @NotNull
    private final StateRef value;

    /**
     * The {@link ContractState} type that the current pointer is pointing to.
     */
    @NotNull
    private final Class<T> type;

    /**
     * Initializes a new instance of the {@link IdentifiablePointer} class.
     *
     * @param value The {@link StateRef} that the current pointer is pointing to.
     * @param type  The {@link ContractState} type that the current pointer is pointing to.
     */
    public IdentifiablePointer(@NotNull final StateRef value, @NotNull final Class<T> type) {
        this.value = value;
        this.type = type;
    }

    /**
     * Gets the {@link StateRef} that the current pointer is pointing to.
     *
     * @return Returns the {@link StateRef} that the current pointer is pointing to.
     */
    @NotNull
    public StateRef getValue() {
        return value;
    }

    /**
     * Gets the {@link ContractState} type that the current pointer is pointing to.
     *
     * @return Returns the {@link ContractState} type that the current pointer is pointing to.
     */
    @NotNull
    public Class<T> getType() {
        return type;
    }

    /**
     * Determines whether the current {@link IdentifiablePointer} is pointing to the specified {@link StateAndRef} instance.
     *
     * @param stateAndRef The {@link StateAndRef} to check.
     * @return Returns true if the current {@link IdentifiablePointer} is pointing to the specified {@link StateAndRef}; otherwise, false.
     */
    @Override
    public boolean isPointingTo(@NotNull final StateAndRef<T> stateAndRef) {
        return Objects.equals(getValue(), stateAndRef.getRef())
                || Objects.equals(getValue(), stateAndRef.getState().getContractState().getId())
                && Objects.equals(getType(), stateAndRef.getState().getContractState().getClass());
    }

    /**
     * Resolves the current {@link StatePointer} to a {@link List} of {@link StateAndRef} of type {@link T}.
     *
     * @param service The {@link UtxoLedgerService} that will be used to resolve {@link StateAndRef} instances of type {@link T} from the vault.
     * @return Returns a {@link List} of {@link StateAndRef} of type {@link T} resolved by this pointer.
     */
    @Override
    public @NotNull List<StateAndRef<T>> resolve(@NotNull UtxoLedgerService service) {
        throw new NotImplementedError("See Dan Newton regarding vault queries");
    }

    /**
     * Resolves the current {@link IdentifiablePointer} to a {@link List} of {@link StateAndRef} of type {@link T}.
     *
     * @param transaction The {@link UtxoLedgerTransaction} from which to resolve {@link ContractState} instances.
     * @param position    The position in the transaction from which to resolve {@link ContractState} instances.
     * @return Returns a {@link List} of {@link StateAndRef} of type {@link T} resolved by this pointer.
     */
    @NotNull
    @Override
    public List<StateAndRef<T>> resolve(
            @NotNull final UtxoLedgerTransaction transaction,
            @NotNull final StatePosition position) {
        return position.getStateAndRefs(transaction, getType());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    public boolean equals(@NotNull final IdentifiablePointer<T> other) {
        return Objects.equals(getValue(), other.getValue())
                && Objects.equals(getType(), other.getType());
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param obj The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(@Nullable final Object obj) {
        return this == obj || obj instanceof IdentifiablePointer<?> && equals((IdentifiablePointer<T>) obj);
    }

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    @Override
    public String toString() {
        return MessageFormat.format("IdentifiablePointer(value = {0}, type = {1})", getValue(), getType());
    }
}

