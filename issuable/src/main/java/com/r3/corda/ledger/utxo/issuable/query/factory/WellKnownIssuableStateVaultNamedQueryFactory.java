package com.r3.corda.ledger.utxo.issuable.query.factory;

import com.r3.corda.ledger.utxo.issuable.query.WellKnownIssuableStateQueries;
import net.corda.v5.ledger.utxo.query.VaultNamedQueryFactory;
import net.corda.v5.ledger.utxo.query.registration.VaultNamedQueryBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class WellKnownIssuableStateVaultNamedQueryFactory implements VaultNamedQueryFactory {

    @Override
    public void create(@NotNull VaultNamedQueryBuilderFactory vaultNamedQueryBuilderFactory) {
        vaultNamedQueryBuilderFactory
                .create(WellKnownIssuableStateQueries.GET_BY_ISSUER_NAME)
                .whereJson(
                        "WHERE visible_states.custom_representation ? 'com.r3.corda.ledger.utxo.issuable.WellKnownIssuableState' " +
                                "AND visible_states.custom_representation -> 'com.r3.corda.ledger.utxo.issuable.WellKnownIssuableState' ->> 'issuerName' = :issuerName " +
                                "AND visible_States.custom_representation ? :stateType " +
                                "AND visible_states.consumed IS NULL"
                )
                .register();
    }
}
