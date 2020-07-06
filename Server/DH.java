import java.math.BigInteger;

public class DH {
    public static final DH BASE_2 = new DH(BigInteger.valueOf(2));
    public static final DH BASE_5 = new DH(BigInteger.valueOf(5));

    private static final long __loadTime = System.currentTimeMillis();

    private final BigInteger _base;

    public DH(BigInteger base)
    {
        _base = base;
    }



    /**
     * Generates a random private Key (element 0) and a random public key (element 1)
     * from the given {@code modulus}.
     *
     * @param modulus
     * @return BigInteger array.  Element 0 is privateKey.  Element 1 is publicKey.
     */
    public BigInteger[] generateRandomKeys(BigInteger modulus)
    {
        BigInteger privateKey = BigInteger.valueOf(System.currentTimeMillis() + __loadTime);
        BigInteger publicKey = generatePublicKey(privateKey, modulus);
        return new BigInteger[]{privateKey, publicKey};
    }

    /**
     * Generates a public key from the given {@code privateKey} and {@code modulus}.
     */
    public BigInteger generatePublicKey(BigInteger privateKey, BigInteger modulus)
    {
        return _base.modPow(privateKey, modulus);
    }

    /**
     * Gets/computes the shared secret key from the given {@code privateKey},
     * {@code modulus} and {@code responseKey} - which is a public key.
     */
    public static BigInteger getSharedSecretKey(BigInteger privateKey, BigInteger modulus,
                                                BigInteger responseKey)
    {
        return responseKey.modPow(privateKey, modulus);
    }

    public static byte[] convert(byte[] original,BigInteger myPrivate,BigInteger otherCode){
        BigInteger secret = getSharedSecretKey(myPrivate,BigInteger.valueOf(77),otherCode);
        byte[] newArr = new byte[original.length];
        for (int i = 0; i < original.length; i++) {
            newArr[i] = (byte) (original[i] ^ secret.intValue());
        }
        return newArr;
    }
    public static byte[] convert(byte[] original, BigInteger secret) {
        byte[] newArr = new byte[original.length];
        for (int i = 0; i < original.length; i++) {
            newArr[i] = (byte) (original[i] ^ secret.intValue());
        }
        return newArr;
    }
    public static byte convert(byte datum,BigInteger myPrivate,BigInteger otherCode) {
        return (byte) (datum ^ getSharedSecretKey(myPrivate,BigInteger.valueOf(77),otherCode).intValue());
    }
}
