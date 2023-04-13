package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.ByteArrays;
import net.corda.v5.crypto.SecureHash;
import net.corda.v5.ledger.utxo.StateRef;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

public final class ContractTestUtils {

    private static final Random random = new Random();

    @NotNull
    public static SecureHash createRandomSecureHash() {
        final int length = 64;
        final StringBuilder builder = new StringBuilder();

        while (builder.length() < length) {
            final String hex = Integer.toHexString(random(0, 255));
            builder.append(hex);
        }

        final byte[] hex = ByteArrays.parseAsHex(builder.substring(0, length));

        return new SecureHash() {
            @NotNull
            @Override
            public String getAlgorithm() {
                return "SHA256";
            }

            @NotNull
            @Override
            public byte[] getBytes() {
                return hex;
            }

            @NotNull
            @Override
            public String toHexString() {
                return ByteArrays.toHexString(hex);
            }
        };
    }

    @NotNull
    public static StateRef createRandomStateRef() {
        final SecureHash transactionId = createRandomSecureHash();
        final int outputIndex = random(0, Integer.MAX_VALUE);

        return new StateRef(transactionId, outputIndex);
    }

    @NotNull
    public static PublicKey createRandomPublicKey() {
        return new PublicKey() {
            @Override
            public String getAlgorithm() {
                return "ECDSA";
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return createRandomSecureHash().getBytes();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj || obj instanceof PublicKey && Arrays.equals(getEncoded(), ((PublicKey) obj).getEncoded());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getAlgorithm(), getFormat(), Arrays.hashCode(getEncoded()));
            }

            @Override
            public String toString() {
                return Base64.getEncoder().encodeToString(getEncoded());
            }
        };
    }

    private static int random(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
