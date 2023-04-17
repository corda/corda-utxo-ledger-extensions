package com.r3.corda.ledger.utxo.issuable.query.factory;

import com.r3.corda.ledger.utxo.issuable.query.IssuableStateQueries;
import net.corda.v5.ledger.utxo.query.VaultNamedQueryFactory;
import net.corda.v5.ledger.utxo.query.registration.VaultNamedQueryBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class IssuableStateVaultNamedQueryFactory implements VaultNamedQueryFactory {

    @Override
    public void create(@NotNull VaultNamedQueryBuilderFactory vaultNamedQueryBuilderFactory) {
        vaultNamedQueryBuilderFactory
                .create(IssuableStateQueries.GET_BY_ISSUER)
                .whereJson(
                        "WHERE visible_states.custom_representation ? 'com.r3.corda.ledger.utxo.issuable.IssuableState' " +
                                "AND visible_states.custom_representation -> 'com.r3.corda.ledger.utxo.issuable.IssuableState' ->> 'issuer' = :issuer " +
                                "AND visible_States.custom_representation ? :stateType " +
                                "AND visible_states.consumed IS NULL"
                )
                .register();
    }
}
