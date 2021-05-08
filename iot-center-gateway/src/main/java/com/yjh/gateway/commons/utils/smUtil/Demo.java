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
    // 国密规范测试私钥
    private static final String prik = "00AFB685CF8993EF80FF9B6F8DD92486710C719AB3820B9D48A13A12ED9FD6CFE1";
    //国密规范测试公钥
    private static final String pubk =  //"0461fb6367aefc6db728b8bd889349c25fac42c94a78c9d564af02feba1613d9cbb5f6a62151941873e5b2428033413ab7502b25dfde03c51bdcc4fb3027cb3bd0";
                                       "048B2E251938FC25FC30F55A485F0FD91376B63CB4BCC863A11A59E59ACC6C802F628E48EA8FA63960956ED5BD817910AF5388E3D0D01379C0830FD789C7ECF47F";



    private static final String pub ="0461fb6367aefc6db728b8bd889349c25fac42c94a78c9d564af02feba1613d9cbb5f6a62151941873e5b2428033413ab7502b25dfde03c51bdcc4fb3027cb3bd0";


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
    public static boolean verify(String summary, String sign,String pub) {
        boolean vs = false; //验签结果
        try {
            vs = SM2Utils.verifySign(userId.getBytes(),  Util.hexToByte(pub), summary.getBytes(), Base64.decode(sign));
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

    }

}
