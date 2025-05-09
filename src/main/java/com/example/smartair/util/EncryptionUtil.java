package com.example.smartair.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {
    // AES 알고리즘을 사용하여 암호화 및 복호화를 수행
    private static final String ALGORITHM = "AES";

    // 암호화 키는 환경 변수에서 가져옴 (32자, 256비트 키 필요)
    private static final String KEY = System.getenv("PAT_SECRET_KEY");

    /**
     * 주어진 평문(plainText)을 AES 알고리즘을 사용하여 암호화
     * @param plainText 암호화할 문자열
     * @return 암호화된 문자열 (Base64 인코딩)
     * @throws Exception 암호화 과정에서 발생할 수 있는 예외
     */
    public static String encrypt(String plainText) throws Exception {
        // AES 알고리즘에 사용할 비밀 키 생성
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);

        // Cipher 객체를 AES 알고리즘으로 초기화 (암호화 모드)
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // 평문을 암호화
        byte[] encrypted = cipher.doFinal(plainText.getBytes());

        // 암호화된 바이트 배열을 Base64 문자열로 변환하여 반환
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 주어진 암호문(encryptedText)을 AES 알고리즘을 사용하여 복호화
     * @param encryptedText 복호화할 암호화된 문자열 (Base64 인코딩)
     * @return 복호화된 평문
     * @throws Exception 복호화 과정에서 발생할 수 있는 예외
     */
    public static String decrypt(String encryptedText) throws Exception {
        // AES 알고리즘에 사용할 비밀 키 생성
        SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);

        // Cipher 객체를 AES 알고리즘으로 초기화 (복호화 모드)
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        // Base64로 인코딩된 암호문을 디코딩한 후 복호화
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

        // 복호화된 바이트 배열을 문자열로 변환하여 반환
        return new String(decrypted);
    }
}