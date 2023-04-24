package com.r3.corda.ledger.utxo.chainable.query.json;

import com.r3.corda.ledger.utxo.chainable.ChainableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

public class ChainableStateVaultJsonFactory implements ContractStateVaultJsonFactory<ChainableState<?>> {

    @NotNull
    @Override
    @SuppressWarnings("UNCHECKED_CAST")
    public Class<ChainableState<?>> getStateType() {
        return (Class<ChainableState<?>>)(Class<?>) ChainableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull ChainableState<?> state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return "{}";
    }
}
