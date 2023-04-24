package com.r3.corda.ledger.utxo.ownable.query.factory;

import com.r3.corda.ledger.utxo.ownable.query.OwnableStateQueries;
import net.corda.v5.ledger.utxo.query.VaultNamedQueryFactory;
import net.corda.v5.ledger.utxo.query.registration.VaultNamedQueryBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class OwnableStateVaultNamedQueryFactory implements VaultNamedQueryFactory {

    @Override
    public void create(@NotNull VaultNamedQueryBuilderFactory vaultNamedQueryBuilderFactory) {
        vaultNamedQueryBuilderFactory
                .create(OwnableStateQueries.GET_BY_OWNER)
                .whereJson(
                        "WHERE visible_states.custom_representation -> 'com.r3.corda.ledger.utxo.ownable.OwnableState' ->> 'owner' = :owner " +
                                "AND visible_States.custom_representation ? :stateType " +
                                "AND visible_states.consumed IS NULL"
                )
                .register();
    }
}
