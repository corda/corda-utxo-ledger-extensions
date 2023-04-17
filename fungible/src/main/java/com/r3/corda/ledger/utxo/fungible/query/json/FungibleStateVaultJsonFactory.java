package com.r3.corda.ledger.utxo.fungible.query.json;

import com.r3.corda.ledger.utxo.fungible.FungibleState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

public class FungibleStateVaultJsonFactory implements ContractStateVaultJsonFactory<FungibleState<?>> {

    @NotNull
    @Override
    @SuppressWarnings("UNCHECKED_CAST")
    public Class<FungibleState<?>> getStateType() {
        return (Class<FungibleState<?>>)(Class<?>) FungibleState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull FungibleState<?> state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return "{}";
    }
}
