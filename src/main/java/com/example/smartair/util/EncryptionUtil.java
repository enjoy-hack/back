package com.example.smartair.util;

import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";

    private final byte[] key;

    public EncryptionUtil(@Value("${pat.secret-key}") String key) {
        if (key == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "암호화 환경 변수 'PAT_SECRET_KEY'가 비어 있습니다.");
        }

        byte[] decodedKey = Base64.getDecoder().decode(key);
        if (decodedKey.length != 32) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,"암호화 환경 변수 'PAT_SECRET_KEY'는 Base64 인코딩된 32바이트(256비트) 값이어야 합니다.");
        }

        this.key = decodedKey;
    }

    public String encrypt(String plainText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedText) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted);
    }
}
