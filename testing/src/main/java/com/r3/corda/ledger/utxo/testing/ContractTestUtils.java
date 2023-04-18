package com.r3.corda.ledger.utxo.testing;

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

    public static byte[] createRandomByteArray(final int length) {
        byte[] result = new byte[length];
        random.nextBytes(result);

        return result;
    }

    @NotNull
    public static SecureHash createRandomSecureHash() {
        return new SecureHash() {
            @NotNull
            @Override
            public String getAlgorithm() {
                return "SHA256";
            }

            @NotNull
            @Override
            public String toHexString() {
                byte[] bytes = createRandomByteArray(32);
                return convertToHexadecimalString(bytes);
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
                return createRandomByteArray(64);
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

    @NotNull
    private static String convertToHexadecimalString(final byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        final char[] hexChars = new char[bytes.length * 2];

        for (int index = 0; index < bytes.length; index++) {
            int value = bytes[index] & 0xFF;
            hexChars[index * 2] = HEX_ARRAY[value >>> 4];
            hexChars[index * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }

        return new String(hexChars);
    }

    private static int random(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
