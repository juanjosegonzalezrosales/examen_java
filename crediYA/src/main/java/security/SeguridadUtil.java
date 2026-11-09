package security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class SeguridadUtil {

    public static final int ITERACIONES_DEFECTO = 65536;
    private static final int LONGITUD_HASH_BITS = 256;
    private static final int LONGITUD_SALT_BYTES = 16;
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SeguridadUtil() {
    }

    public static String generarSalt() {
        byte[] salt = new byte[LONGITUD_SALT_BYTES];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public static String hashearPassword(char[] password, String saltHex, int iteraciones) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("La contraseña y el salt no pueden ser nulos.");
        }

        try {
            byte[] salt = HexFormat.of().parseHex(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, LONGITUD_HASH_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITMO);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Error al calcular el hash de contraseña: " + e.getMessage(), e);
        }
    }

    public static boolean verificarPassword(char[] password, String saltHex, int iteraciones, String hashEsperado) {
        if (password == null || saltHex == null || hashEsperado == null) {
            return false;
        }

        String hashCalculado = hashearPassword(password, saltHex, iteraciones);
        byte[] a = HexFormat.of().parseHex(hashCalculado);
        byte[] b = HexFormat.of().parseHex(hashEsperado);
        return MessageDigest.isEqual(a, b);
    }
}
