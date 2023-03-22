package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.ledger.utxo.TimeWindow;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

final class TimeWindowBetweenImpl implements TimeWindow {

    @NotNull
    private final Instant from;

    @NotNull
    private final Instant until;

    public TimeWindowBetweenImpl(@NotNull Instant from, @NotNull Instant until) {
        this.from = from;
        this.until = until;
    }

    @NotNull
    @Override
    public Instant getFrom() {
        return from;
    }

    @NotNull
    @Override
    public Instant getUntil() {
        return until;
    }

    @Override
    public boolean contains(@NotNull final Instant instant) {
        return instant.isAfter(from) && instant.isBefore(until);
    }
}
