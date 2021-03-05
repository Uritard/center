package com.yjh.logs.common.smUtil;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @Description: TODO(国密SM2签名验签 / SM3报文摘要)
 */
public class Demo {

    // 国密规范测试用户ID
    private static final String userId = "rzx";
    // 国密规范测试私钥
    private static final String prik = "00AFB685CF8993EF80FF9B6F8DD92486710C719AB3820B9D48A13A12ED9FD6CFE1";
    //国密规范测试公钥
    private static final String pubk ="048B2E251938FC25FC30F55A485F0FD91376B63CB4BCC863A11A59E59ACC6C802F628E48EA8FA63960956ED5BD817910AF5388E3D0D01379C0830FD789C7ECF47F";


//    public static void main(String[] arg) {
//        String msg = "123456789";//原始数据
//        System.out.println("原始数据：" + msg);
//        String summaryString = summary(String.valueOf(msg));
//        System.out.println("摘要：" + summaryString);
//        String signString = sign(summaryString);
//        System.out.println("摘要签名：" + signString);
//        boolean status = verify(summaryString, signString);
//        System.out.println("验签结果：" + status);
//
//        System.out.println("加密: ");
//        byte[] cipherText = null;
//        try {
//            cipherText = SM2Utils.encrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(pubk))).getBytes()), String.valueOf(msg).getBytes());
//        } catch (IllegalArgumentException e1) {
//            // TODO 自动生成的 catch 块
//            e1.printStackTrace();
//        } catch (IOException e1) {
//            // TODO 自动生成的 catch 块
//            e1.printStackTrace();
//        }
//        System.out.println(new String(Base64.encode(cipherText)));
//        System.out.println("");
//
//        System.out.println("解密: ");
//        String res = null;
//        try {
//            res = new String(SM2Utils.decrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(prik))).getBytes()), cipherText));
//        } catch (IllegalArgumentException e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO 自动生成的 catch 块
//            e.printStackTrace();
//        }
//        System.out.println(res);
//
//    }

    /**
     * 摘要
     *
     * @return
     */
    public static String summary(String msg) {
        //1.摘要
        byte[] md = new byte[32];
        SM3Digest sm = new SM3Digest();
        sm.update(msg.getBytes(), 0, msg.getBytes().length);
        sm.doFinal(md, 0);
        String s = new String(Hex.encode(md));
        return s;
    }

    /**
     * 签名
     *
     * @return
     */
    public static String sign(String summaryString) {
        String prikS = new String(Base64.encode(Util.hexToByte(prik)));
        System.out.println("prikS: " + prikS);
        System.out.println("");

        System.out.println("ID: " + Util.getHexString(userId.getBytes()));
        System.out.println("");
        System.out.println("签名: ");
        byte[] sign = null; //摘要签名
        try {
            sign = SM2Utils.sign(userId.getBytes(), Base64.decode(prikS.getBytes()), Util.hexToByte(summaryString));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Util.getHexString(sign);
    }

    /**
     * 验签
     *
     * @return
     */
    public static boolean verify(String summary, String sign) {
        String pubkS = new String(Base64.encode(Util.hexToByte(pubk)));
        System.out.println("pubkS: " + pubkS);
        System.out.println("");

        System.out.println("验签 ");
        boolean vs = false; //验签结果
        try {
            vs = SM2Utils.verifySign(userId.getBytes(), Base64.decode(pubkS.getBytes()), Util.hexToByte(summary), Util.hexToByte(sign));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vs;
    }

    /**
     * 生成随机密钥对
     */
    public static void createKey() {
        SM2 sm2 = SM2.Instance();
        AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
        ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
        ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
        BigInteger privateKey = ecpriv.getD();
        ECPoint publicKey = ecpub.getQ();

        System.out.println("公钥: " + Util.byteToHex(publicKey.getEncoded()));
        System.out.println("私钥: " + Util.byteToHex(privateKey.toByteArray()));
    }
    /**
     * 加密
     */
//    public static  String encryption(String msg) throws IOException {
//      return   new String(Base64.encode(SM2Utils.encrypt(Base64.decode(new String(Base64.encode(Util.hexToByte(pubk))).getBytes()), msg.getBytes())));
//    }

    /**
     * 解密
     */
//    public  static  String decrypt(String msg) throws IOException {
//        if (StringUtils.isNotBlank(msg)) {
//            return new String(SM2Utils.decrypt(org.bouncycastle.util.encoders.Base64.decode(new String(org.bouncycastle.util.encoders.Base64.encode(Util.hexToByte(prik))).getBytes()), Base64.decode(msg.getBytes())));
//        }else {
//            return  msg;
//        }
//    }


    /**
     * 解密前端密码
     *
     * @param pCode 前端密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String decrypt(String pCode) throws IOException {
        if(StringUtils.isNoneBlank(pCode)){
            return new String(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte("04" + pCode)));
        }else {
            return  "";
        }
    }

    /**
     * 解密后端密码
     *
     * @param pCode 后端密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String decryptDB(String pCode) throws IOException {
        if(StringUtils.isNoneBlank(pCode)) {
            return new String(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte(pCode)));
        }else {
            return  "";
        }
    }

    /**
     * 加密
     *
     * @param pCode 密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String encryption(String pCode) throws IOException {
        return SM2Utils.encrypt(Util.hexStringToBytes(pubk), pCode.getBytes());
    }
}
