package com.r3.corda.ledger.utxo.common;

import org.jetbrains.annotations.*;

import java.lang.reflect.*;

abstract class TypeReference<T> implements Comparable<TypeReference<T>> {

    @NotNull
    final Type type;

    protected TypeReference() {
        Type superClass = getClass().getGenericSuperclass();

        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }

        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }

    @Override
    public final int compareTo(@NotNull TypeReference<T> o) {
        return 0;
    }
}
