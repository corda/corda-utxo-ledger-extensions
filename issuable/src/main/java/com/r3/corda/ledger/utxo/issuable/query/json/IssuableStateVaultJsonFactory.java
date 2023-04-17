package com.r3.corda.ledger.utxo.issuable.query.json;

import com.r3.corda.ledger.utxo.issuable.IssuableState;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.util.ByteArrays;
import net.corda.v5.crypto.DigestAlgorithmName;
import net.corda.v5.ledger.utxo.query.json.ContractStateVaultJsonFactory;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class IssuableStateVaultJsonFactory implements ContractStateVaultJsonFactory<IssuableState> {

    @NotNull
    @Override
    public Class<IssuableState> getStateType() {
        return IssuableState.class;
    }

    @NotNull
    @Override
    public String create(@NotNull IssuableState state, @NotNull JsonMarshallingService jsonMarshallingService) {
        return jsonMarshallingService.format(new IssuableStateJson(getPublicKeyId(state.getIssuer())));
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

    static class IssuableStateJson {

        @NotNull
        private final String issuer;

        public IssuableStateJson(@NotNull String issuer) {
            this.issuer = issuer;
        }

        @NotNull
        public String getIssuer() {
            return issuer;
        }
    }
}
