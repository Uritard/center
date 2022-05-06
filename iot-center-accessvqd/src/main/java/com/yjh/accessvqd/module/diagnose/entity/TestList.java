package com.yjh.accessvqd.module.diagnose.entity;

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


public class TestList {
	
	//���ܷ�ʽ
    public static String KEY_ALGORITHM = "AES";
    //������䷽ʽ
    String algorithmStr = "AES/CBC/PKCS7Padding";
    //�����ظ�new���ɶ��BouncyCastleProvider������ΪGC���ղ��ˣ�������ڴ����
    //ֻ�ڵ�һ�ε���decrypt()����ʱ��new ����
    public static boolean initialized = false;

    /**
     * 
     * @param originalContent
     * @param encryptKey
     * @param ivByte
     * @return
     */
    public byte[] encrypt(byte[] originalContent, byte[] encryptKey, byte[] ivByte) {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");//  AES/CBC/PKCS5Padding
            SecretKeySpec skeySpec = new SecretKeySpec(encryptKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(ivByte));
            byte[] encrypted = cipher.doFinal(originalContent);
            // System.out.println(Arrays.toString(encrypted));
            return encrypted;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES����
     * ���ģʽAES/CBC/PKCS7Padding
     * ����ģʽ128
     * @param content
     *            Ŀ������
     * @return
     * @throws Exception 
     * @throws InvalidKeyException 
     * @throws NoSuchProviderException
     */
    public byte[] decrypt(byte[] content, byte[] aesKey, byte[] ivByte) {
        initialize();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            Key sKeySpec = new SecretKeySpec(aesKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, generateIV(ivByte));// ��ʼ��
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**BouncyCastle��Ϊ��ȫ�ṩ����ֹ���Ǽ��ܽ���ʱ����Ϊjdk���õĲ�֧�ָ�ģʽ���б���**/
    public static void initialize() {
        if (initialized)
            return;
        Security.addProvider(new BouncyCastleProvider());
        initialized = true;
    }

    // ����iv
    public static AlgorithmParameters generateIV(byte[] iv) throws Exception {
        AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
        params.init(new IvParameterSpec(iv));
        return params;
    }
	
	public static void main(String[] args) {
		// String pass = "12345";
		// String key = "ivms6@hikvision$";
		// String iv="8807599889957088";
		// TestList test = new TestList();
		// byte[] js = test.encrypt(pass.getBytes(),key.getBytes(),iv.getBytes());
		// System.out.println(Base64.getEncoder().encodeToString(js));
	}

}
