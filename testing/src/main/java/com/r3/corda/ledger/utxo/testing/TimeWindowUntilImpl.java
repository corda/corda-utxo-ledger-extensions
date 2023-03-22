package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.utxo.TimeWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

final class TimeWindowUntilImpl implements TimeWindow {

    @NotNull
    private final Instant until;

    public TimeWindowUntilImpl(@NotNull Instant until) {
        this.until = until;
    }

    @Nullable
    @Override
    public Instant getFrom() {
        return null;
    }

    @NotNull
    @Override
    public Instant getUntil() {
        return until;
    }

    @Override
    public boolean contains(@NotNull Instant instant) {
        return instant.isBefore(until);
    }
}
