package com.r3.corda.ledger.utxo.base;

import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Provides functionality for contract verification checks.
 */
@SuppressWarnings("unused")
public final class Check {

    /**
     * Prevents instance of {@link Check} from being initialized.
     */
    private Check() {
    }

    /**
     * Checks whether the specified condition is true.
     *
     * @param condition The condition to test.
     * @param message   The message which will be thrown in the event that the condition is false.
     * @throws IllegalStateException if the condition is false.
     */
    public static void isTrue(final boolean condition, @NotNull final String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    /**
     * Checks whether the specified condition is false.
     *
     * @param condition The condition to test.
     * @param message   The message which will be thrown in the event that the condition is true.
     * @throws IllegalStateException if the condition is true.
     */
    public static void isFalse(final boolean condition, @NotNull final String message) {
        if (condition) throw new IllegalStateException(message);
    }

    /**
     * Checks whether the specified value is null.
     *
     * @param value   The value to test.
     * @param message The message which will be thrown in the event that the value is not null.
     * @throws IllegalStateException if the condition is not null.
     */
    public static void isNull(@Nullable final Object value, @NotNull final String message) {
        isTrue(Objects.isNull(value), message);
    }

    /**
     * Checks whether the specified value is not null.
     *
     * @param value   The value to test.
     * @param message The message which will be thrown in the event that the value is null.
     * @throws IllegalStateException if the condition is null.
     */
    public static void isNotNull(@Nullable final Object value, @NotNull final String message) {
        isTrue(Objects.nonNull(value), message);
    }

    /**
     * Checks whether the specified values are equal.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the values are not equal.
     * @throws IllegalStateException if the values are not equal.
     */
    public static void isEqual(@NotNull final Object left, @NotNull final Object right, @NotNull final String message) {
        isTrue(Objects.equals(left, right), message);
    }

    /**
     * Checks whether the specified values are not equal.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the values are equal.
     * @throws IllegalStateException if the values are equal.
     */
    public static void isNotEqual(@NotNull final Object left, @NotNull final Object right, @NotNull final String message) {
        isTrue(!Objects.equals(left, right), message);
    }

    /**
     * Checks whether the specified left-hand value is greater than the specified right-hand value.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the left-hand value is not greater than the right-hand value.
     * @param <T>     The underlying {@link Comparable} type to be compared.
     * @throws IllegalStateException if the left-hand value is not greater than the right-hand value.
     */
    public static <T extends Comparable<T>> void isGreaterThan(@NotNull final T left, @NotNull final T right, @NotNull final String message) {
        isTrue(left.compareTo(right) > 0, message);
    }

    /**
     * Checks whether the specified left-hand value is greater than or equal to the specified right-hand value.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the left-hand value is not greater than or equal to the right-hand value.
     * @param <T>     The underlying {@link Comparable} type to be compared.
     * @throws IllegalStateException if the left-hand value is not greater than or equal to the right-hand value.
     */
    public static <T extends Comparable<T>> void isGreaterThanOrEqual(@NotNull final T left, @NotNull final T right, @NotNull final String message) {
        isTrue(left.compareTo(right) >= 0, message);
    }

    /**
     * Checks whether the specified left-hand value is less than the specified right-hand value.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the left-hand value is not less than the right-hand value.
     * @param <T>     The underlying {@link Comparable} type to be compared.
     * @throws IllegalStateException if the left-hand value is not less than the right-hand value.
     */
    public static <T extends Comparable<T>> void isLessThan(@NotNull final T left, @NotNull final T right, @NotNull final String message) {
        isTrue(left.compareTo(right) < 0, message);
    }

    /**
     * Checks whether the specified left-hand value is less than or equal to the specified right-hand value.
     *
     * @param left    The left-hand value to test.
     * @param right   The right-hand value to test.
     * @param message The message which will be thrown in the event that the left-hand value is not less than or equal to the right-hand value.
     * @param <T>     The underlying {@link Comparable} type to be compared.
     * @throws IllegalStateException if the left-hand value is not less than or equal to the right-hand value.
     */
    public static <T extends Comparable<T>> void isLessThanOrEqual(@NotNull final T left, @NotNull final T right, @NotNull final String message) {
        isTrue(left.compareTo(right) <= 0, message);
    }

    /**
     * Checks whether any elements in the specified iterable match the specified predicate.
     *
     * @param iterable  The iterable containing elements to test.
     * @param predicate The condition to test each element against.
     * @param message   The message which will be thrown in the event that no elements match the specified predicate.
     * @param <T>       The underlying type of the iterable elements.
     * @throws IllegalStateException if no elements match the specified predicate.
     */
    public static <T> void any(@NotNull final Iterable<T> iterable, @NotNull final Predicate<T> predicate, @NotNull final String message) {
        for (final T element : iterable) {
            if (predicate.test(element)) return;
        }

        throw new IllegalStateException(message);
    }

    /**
     * Checks whether all elements in the specified iterable match the specified predicate.
     *
     * @param iterable  The iterable containing elements to test.
     * @param predicate The condition to test each element against.
     * @param message   The message which will be thrown in the event that some elements do not match the specified predicate.
     * @param <T>       The underlying type of the iterable elements.
     * @throws IllegalStateException if some elements do not match the specified predicate.
     */
    public static <T> void all(@NotNull final Iterable<T> iterable, @NotNull final Predicate<T> predicate, @NotNull final String message) {
        for (final T element : iterable) {
            isTrue(predicate.test(element), message);
        }
    }

    /**
     * Checks whether no elements in the specified iterable match the specified predicate.
     *
     * @param iterable  The iterable containing elements to test.
     * @param predicate The condition to test each element against.
     * @param message   The message which will be thrown in the event that some elements match the specified predicate.
     * @param <T>       The underlying type of the iterable elements.
     * @throws IllegalStateException if some elements match the specified predicate.
     */
    public static <T> void none(@NotNull final Iterable<T> iterable, @NotNull final Predicate<T> predicate, @NotNull final String message) {
        for (final T element : iterable) {
            isTrue(!predicate.test(element), message);
        }
    }

    /**
     * Checks whether the specified iterable is empty.
     *
     * @param iterable The iterable containing elements to test.
     * @param message  The message which will be thrown in the event that the iterable is not empty.
     * @throws IllegalStateException if the iterable is not empty.
     */
    public static void isEmpty(@NotNull final Iterable<?> iterable, @NotNull final String message) {
        isTrue(count(iterable) == 0, message);
    }

    /**
     * Checks whether the specified iterable is not empty.
     *
     * @param iterable The iterable containing elements to test.
     * @param message  The message which will be thrown in the event that the iterable is empty.
     * @throws IllegalStateException if the iterable is empty.
     */
    public static void isNotEmpty(@NotNull final Iterable<?> iterable, @NotNull final String message) {
        isTrue(count(iterable) > 0, message);
    }

    /**
     * Checks whether the specified iterable contains distinct elements.
     *
     * @param iterable The iterable containing elements to test.
     * @param message  The message which will be thrown in the event that the iterable does not contain distinct elements.
     * @throws IllegalStateException if the iterable does not contain distinct elements.
     */
    public static void isDistinct(@NotNull final Iterable<?> iterable, @NotNull final String message) {
        final Set<?> distinctItems = StreamSupport
                .stream(iterable.spliterator(), false)
                .collect(Collectors.toSet());

        final int originalCount = count(iterable);
        final int distinctCount = count(distinctItems);

        isTrue(originalCount == distinctCount, message);
    }

    /**
     * Checks whether a transaction contains a single command of the specified type.
     *
     * @param transaction The transaction from which to obtain commands.
     * @param type        The type of command to obtain from the transaction.
     * @param message     The message which will be thrown in the event that the transaction either contains zero, or greater than one commands of the specified type.
     * @param <T>         The underlying type of the command to check.
     * @return Returns a single command of the specified type.
     * @throws IllegalStateException if the transaction either contains zero, or greater than one commands of the specified type.
     */
    public static <T extends Command> T checkSingleCommand(@NotNull final UtxoLedgerTransaction transaction, @NotNull final Class<T> type, @NotNull final String message) {
        List<T> commands = transaction.getCommands(type);

        isTrue(commands.size() == 1, message);

        return commands.get(0);
    }

    /**
     * Counts the number of elements in an iterable.
     *
     * @param iterable The iterable to count.
     * @return Returns the number of elements in an iterable.
     */
    private static int count(@NotNull final Iterable<?> iterable) {

        if (iterable instanceof Collection<?>) {
            return ((Collection<?>) iterable).size();
        }

        int result = 0;

        for (final Object element : iterable) {
            result++;
        }

        return result;
    }
}
