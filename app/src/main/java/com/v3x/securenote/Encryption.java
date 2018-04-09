package com.v3x.securenote;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    public static byte[] SHAHash(String data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        digest.reset();
        return digest.digest(data.getBytes());
    }

    public static String AesDecrypt(String hexKey, String hexEncoded, String iv) {
        try {
            SecretKeySpec secret = new SecretKeySpec(toByte(hexKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(toByte(iv)));
            byte[] decrypted = cipher.doFinal(toByte(hexEncoded));
            return new String(decrypted, "UTF-8");
        } catch (Exception e){
            Log.e("ENC",e.toString());
            return "";
        }
    }
    public static String AesEncrypt(String hexKey, String clear, String iv){
        try {
            SecretKeySpec secret = new SecretKeySpec(toByte(hexKey), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(toByte(iv)));
            byte[] encrypted = cipher.doFinal(toByte(clear));
            return toHex(encrypted);
        } catch (Exception e){
            Log.e("ENC",e.toString());
            return "";
        }
    }

    public static String cryptrand(int len){
        SecureRandom rand = new SecureRandom();
        byte random_bytes[] = new byte[len];
        rand.nextBytes(random_bytes);

        return toHex(random_bytes);
    }

    public static String toHex(String txt) {
        try {
            return toHex(txt.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return toHex(txt.getBytes());
        }
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789abcdef";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

}