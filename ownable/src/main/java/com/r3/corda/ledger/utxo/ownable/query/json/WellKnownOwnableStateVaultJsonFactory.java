package com.r3.corda.ledger.utxo.ownable.query.json;

import com.r3.corda.ledger.utxo.ownable.WellKnownOwnableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

public class WellKnownOwnableStateVaultJsonFactory implements ContractStateVaultJsonFactory<WellKnownOwnableState> {

    @NotNull
    @Override
    public Class<WellKnownOwnableState> getStateType() {
        return WellKnownOwnableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull WellKnownOwnableState state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return jsonMarshallingService.format(new WellKnownOwnableStateJson(state.getOwnerName().toString()));
    }

    static class WellKnownOwnableStateJson {

        @NotNull
        private final String ownerName;

        public WellKnownOwnableStateJson(@NotNull String ownerName) {
            this.ownerName = ownerName;
        }

        @NotNull
        public String getOwnerName() {
            return ownerName;
        }
    }
}
