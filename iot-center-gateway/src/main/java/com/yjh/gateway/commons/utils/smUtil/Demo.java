package com.yjh.gateway.commons.utils.smUtil;

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
    private static final String userId ="1234567812345678";
            //"rzx";
    // 国密规范测试私钥
    private static final String prik = "055C74CFB227BD9CDFF242D233096BC6FDBAFB59D001D5EE7F857ADFC6BF1501";
    //国密规范测试公钥
    private static final String pubk =  "0461fb6367aefc6db728b8bd889349c25fac42c94a78c9d564af02feba1613d9cbb5f6a62151941873e5b2428033413ab7502b25dfde03c51bdcc4fb3027cb3bd0";
            //"04673FC4F3D41C9470E32AABCB5A958E2CE528959F373D0F7AB2B82E65BF4DE8FB67716A269993585451888C8450E92A75A6C34EDFF748097BEAD8E41C2976E8AA";

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
        return s.toUpperCase();
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
//        String pubkS = new String(Base64.encode(Util.hexToByte(pubk)));
//        System.out.println("pubkS: " + pubkS);
//        System.out.println("");
//
//        System.out.println("验签 ");
        boolean vs = false; //验签结果
        try {
            vs = SM2Utils.verifySign(userId.getBytes(),  Util.hexToByte(pubk), summary.getBytes(), Base64.decode(sign));
           // vs = SM2Utils.verifySign(userId.getBytes(), Base64.decode(pubkS.getBytes()), Util.hexToByte(summary), Util.hexToByte(sign));
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

}
