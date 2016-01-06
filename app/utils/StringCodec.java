package utils;

import org.apache.commons.codec.binary.Base64;
import play.Logger;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Created by coder on 1/9/15.
 */
public class StringCodec {
    private static final Logger.ALogger LOGGER = Logger.of(StringCodec.class);
    private static final byte[] KEY = "a9b8c7d6".getBytes();
    private static final DESKeySpec DKS;
    private static final SecretKeyFactory KEY_FACTORY;
    private static final Key SECRET_KEY;
    private static final IvParameterSpec IV = new IvParameterSpec(KEY);
    private static final String CIPHER_INSTANCE_NAME = "DES/CBC/PKCS5Padding";
    private static final Charset UTF_8;

    static {
        try {
            UTF_8 = Charset.forName("UTF-8");
            DKS = new DESKeySpec(KEY);
            KEY_FACTORY = SecretKeyFactory.getInstance("DES");
            SECRET_KEY = KEY_FACTORY.generateSecret(DKS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encode(String s) {
        try {
            Cipher cipherEncode;
            cipherEncode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            cipherEncode.init(Cipher.ENCRYPT_MODE, SECRET_KEY, IV);
            return Base64.encodeBase64String(cipherEncode.doFinal(s.getBytes(UTF_8)));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            LOGGER.error("salary codec encode " + s + " error.", e);
            return null;
        }
    }

    public static String decode(String str) {
        try {
            Cipher cipherDecode;
            cipherDecode = Cipher.getInstance(CIPHER_INSTANCE_NAME);
            cipherDecode.init(Cipher.DECRYPT_MODE, SECRET_KEY, IV);
            return new String(cipherDecode.doFinal(Base64.decodeBase64(str)), UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
                NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            LOGGER.error("salary codec decode " + str + " error.", e);
            return null;
        }
    }

    public static String salt(String str) {
        byte[] payload = str.getBytes();
        byte f = (byte) (sum(payload, 0, payload.length / 2) % 26 + 97);
        byte l = (byte) (sum(payload, payload.length / 2, payload.length) % 26 + 97);
        byte[] bytes = new byte[payload.length + 2];
        bytes[0] = f;
        System.arraycopy(payload, 0, bytes, 1, payload.length);
        bytes[bytes.length - 1] = l;
        return new String(bytes);
    }

    private static int sum(byte[] payload, int start, int end) {
        int sum = 0;
        for (; start < end; start++) {
            sum += payload[start];
        }
        return sum;
    }
}
