package com.r3.corda.ledger.utxo.ownable.query.factory;

import com.r3.corda.ledger.utxo.ownable.query.WellKnownOwnableStateQueries;
import net.corda.v5.ledger.utxo.query.VaultNamedQueryFactory;
import net.corda.v5.ledger.utxo.query.registration.VaultNamedQueryBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class WellKnownOwnableStateVaultNamedQueryFactory implements VaultNamedQueryFactory {

    @Override
    public void create(@NotNull VaultNamedQueryBuilderFactory vaultNamedQueryBuilderFactory) {
        vaultNamedQueryBuilderFactory
                .create(WellKnownOwnableStateQueries.GET_BY_OWNER_NAME)
                .whereJson(
                        "WHERE visible_states.custom_representation -> 'com.r3.corda.ledger.utxo.ownable.WellKnownOwnableState' ->> 'ownerName' = :ownerName " +
                                "AND visible_States.custom_representation ? :stateType " +
                                "AND visible_states.consumed IS NULL"
                )
                .register();
    }
}
