package com.r3.corda.ledger.utxo.base;

import net.corda.v5.ledger.utxo.Command;
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction;
import org.jetbrains.annotations.NotNull;

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
