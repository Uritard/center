package com.yjh.Manager.common;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;


public class TripleDESEncrypt {

    private static final String KEY = "yjh";

    public static String encrypt(String data) throws Exception {
        return encrypt(data, KEY);
    }

    public static String decrypt(String data) {
        try {
            return decrypt(data, KEY);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes("utf-8"), key);
        String strs = new String(Base64.encode(bt), "utf-8");
        return strs;
    }

    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws Exception {
        if (data == null)
            return null;

        byte[] buf = Base64.decode(data.getBytes("utf-8"));
        byte[] bt = decrypt(buf, key);
        return new String(bt, "utf-8");
    }

    /**
     * 加密采用的算法.
     */
    private static final String algorithm = "DESede/CBC/PKCS5Padding";
    /**
     * 密钥采用的算法.
     */
    private static final String algorithm_key = "DESede";
    /**
     * 密钥的长度.
     */
    private static final int KEY_SIZE = 24;

    private static IvParameterSpec IV = new IvParameterSpec(new byte[]{21, 'w', 'i', 's', 53, 'e', 'd', 'u'});


    /**
     * Description 根据键值进行加密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, String key) throws Exception {

        Cipher c = getCipher(Cipher.ENCRYPT_MODE, key);

        return c.doFinal(data);
    }

    private static Cipher getCipher(int model, String key) throws Exception {
        Cipher c = Cipher.getInstance(algorithm);
        c.init(model, makeKey(key), IV);
        return c;
    }

    private static SecretKey makeKey(String key) {
        byte[] buf = new byte[KEY_SIZE];
        int len = key.length();
        for (int i = 0, p = 0; i < len; i++) {
            buf[p] ^= (byte) (key.charAt(i) & 0xff);
            if (++p >= KEY_SIZE) {
                p = 0;
            }
        }
        return new SecretKeySpec(buf, algorithm_key);
    }

    /**
     * Description 根据键值进行解密
     *
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, String key) throws Exception {

        Cipher c = Cipher.getInstance(algorithm);

        c.init(Cipher.DECRYPT_MODE, makeKey(key), IV);

        return c.doFinal(data);
    }

    public static void main(String[] args) throws Exception {
        String s = TripleDESEncrypt.encrypt("zxm10@@@");
        System.out.println(s);
        System.out.println(TripleDESEncrypt.decrypt(s));
    }
}
