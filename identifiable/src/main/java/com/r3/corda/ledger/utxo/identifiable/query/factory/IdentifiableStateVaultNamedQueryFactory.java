package com.r3.corda.ledger.utxo.identifiable.query.factory;

import com.r3.corda.ledger.utxo.identifiable.query.IdentifiableStateQueries;
import net.corda.v5.ledger.utxo.query.VaultNamedQueryFactory;
import net.corda.v5.ledger.utxo.query.registration.VaultNamedQueryBuilderFactory;
import org.jetbrains.annotations.NotNull;

public class IdentifiableStateVaultNamedQueryFactory implements VaultNamedQueryFactory {

    @Override
    public void create(@NotNull VaultNamedQueryBuilderFactory vaultNamedQueryBuilderFactory) {
        vaultNamedQueryBuilderFactory
                .create(IdentifiableStateQueries.GET_BY_IDS)
                .whereJson(
                        "WHERE (" +
                                "   visible_states.custom_representation -> 'com.r3.corda.ledger.utxo.identifiable.IdentifiableState' ->> 'id' IN :ids " +
                                "   OR visible_states.custom_representation -> 'net.corda.v5.ledger.utxo.ContractState' ->> 'stateRef' IN :ids " +
                                ") " +
                                "AND visible_states.consumed IS NULL"
                )
                .register();
    }
}
