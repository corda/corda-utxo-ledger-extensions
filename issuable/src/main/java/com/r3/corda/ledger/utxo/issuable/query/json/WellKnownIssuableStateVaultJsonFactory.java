package com.r3.corda.ledger.utxo.issuable.query.json;

import com.r3.corda.ledger.utxo.issuable.WellKnownIssuableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

public class WellKnownIssuableStateVaultJsonFactory implements ContractStateVaultJsonFactory<WellKnownIssuableState> {

    @NotNull
    @Override
    public Class<WellKnownIssuableState> getStateType() {
        return WellKnownIssuableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull WellKnownIssuableState state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return jsonMarshallingService.format(new WellKnownIssuableStateJson(state.getIssuerName().toString()));
    }

    static class WellKnownIssuableStateJson {

        @NotNull
        private final String issuerName;

        public WellKnownIssuableStateJson(@NotNull String issuerName) {
            this.issuerName = issuerName;
        }

        @NotNull
        public String getIssuerName() {
            return issuerName;
        }
    }
}
