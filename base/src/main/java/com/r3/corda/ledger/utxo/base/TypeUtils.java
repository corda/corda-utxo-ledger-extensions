package com.r3.corda.ledger.utxo.base;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Obtains the {@link Class} type from a generic parameter, if the type is reified at compile-time.
     *
     * @param type  The type from which to obtain a generic parameter type.
     * @param index The index of the generic parameter type to obtain.
     * @param <T>   The underlying type of the generic parameter to obtain.
     * @return Returns the {@link Class} type from a generic parameter, if the type is reified at compile-time.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getGenericArgumentType(@NotNull final Class<?> type, final int index) {
        Type genericSuperclass = type.getGenericSuperclass();
        Check.isFalse(genericSuperclass instanceof Class<?>, "Type constructed without actual type information.");

        return (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[index];
    }

    /**
     * Obtains the {@link Class} type from a generic parameter, if the type is reified at compile-time.
     *
     * @param type The type from which to obtain a generic parameter type.
     * @param <T>  The underlying type of the generic parameter to obtain.
     * @return Returns the {@link Class} type from a generic parameter, if the type is reified at compile-time.
     */
    @NotNull
    public static <T> Class<T> getGenericArgumentType(@NotNull final Class<?> type) {
        return getGenericArgumentType(type, 0);
    }
}
