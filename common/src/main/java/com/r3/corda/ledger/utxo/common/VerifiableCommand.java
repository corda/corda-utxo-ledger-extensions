package com.r3.corda.ledger.utxo.common;

import net.corda.v5.ledger.utxo.*;
import net.corda.v5.ledger.utxo.transaction.*;
import org.jetbrains.annotations.*;

/**
 * Defines a mechanism for delegating verification logic to contract commands.
 */
public interface VerifiableCommand extends Command {

    /**
     * Verifies the specified transaction associated with the current command.
     *
     * @param transaction The transaction to verify.
     */
    void verify(@NotNull UtxoLedgerTransaction transaction);
}
