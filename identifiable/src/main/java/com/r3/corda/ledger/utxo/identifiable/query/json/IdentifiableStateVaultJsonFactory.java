package com.r3.corda.ledger.utxo.identifiable.query.json;

import com.r3.corda.ledger.utxo.identifiable.IdentifiableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.ledger.utxo.StateRef;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IdentifiableStateVaultJsonFactory implements ContractStateVaultJsonFactory<IdentifiableState> {

    @NotNull
    @Override
    public Class<IdentifiableState> getStateType() {
        return IdentifiableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull IdentifiableState state, @NotNull JsonMarshallingService jsonMarshallingService) {
        StateRef id = state.getId();
        return jsonMarshallingService.format(new IdentifiableStateJson(id != null ? id.toString() : null));
    }

    static class IdentifiableStateJson {

        @Nullable
        private final String id;

        public IdentifiableStateJson(@Nullable String id) {
            this.id = id;
        }

        @Nullable
        public String getId() {
            return id;
        }
    }
}
