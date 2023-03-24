package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.utxo.EncumbranceGroup;
import org.jetbrains.annotations.NotNull;

final class EncumbranceGroupImpl implements EncumbranceGroup {

    private final int size;

    @NotNull
    private final String tag;

    public EncumbranceGroupImpl(int size, @NotNull String tag) {
        this.size = size;
        this.tag = tag;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    @NotNull
    public String getTag() {
        return tag;
    }
}
