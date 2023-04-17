package com.r3.corda.ledger.utxo.ownable.query.json;

import com.r3.corda.ledger.utxo.ownable.OwnableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.util.ByteArrays;
import net.corda.v5.crypto.DigestAlgorithmName;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class OwnableStateVaultJsonFactory implements ContractStateVaultJsonFactory<OwnableState> {

    @NotNull
    @Override
    public Class<OwnableState> getStateType() {
        return OwnableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull OwnableState state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return jsonMarshallingService.format(new OwnableStateJson(getPublicKeyId(state.getOwner())));
    }

    /*
    TODO Remove direct access to MessageDigest and use DigestService instead
    This is a temporary change until Corda 5.1 has property injection into {@link ContractStateVaultJsonFactory}s. A {@link DigestService}
    could then be injected into instances of the factory and used to create public key hashes.

    This code should not be replicated within other CorDapps.
     */
    private String getPublicKeyId(PublicKey publicKey) {
        try {
            byte[] bytes = MessageDigest.getInstance(DigestAlgorithmName.SHA2_256.getName()).digest(publicKey.getEncoded());
            String hexString = ByteArrays.toHexString(bytes);
            return "SHA-256:" + hexString;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static class OwnableStateJson {

        @NotNull
        private final String owner;

        public OwnableStateJson(@NotNull String owner) {
            this.owner = owner;
        }

        @NotNull
        public String getOwner() {
            return owner;
        }
    }
}
