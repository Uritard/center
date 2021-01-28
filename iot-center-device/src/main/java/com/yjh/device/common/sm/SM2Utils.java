package com.yjh.device.common.sm;

import org.bouncycastle.asn1.*;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;

import static com.yjh.device.common.Constant.PRI_KEY;
import static com.yjh.device.common.Constant.PUB_KEY;

public class SM2Utils {
    //数据加密
    public static String encrypt(byte[] publicKey, byte[] data) throws IOException {
        if (publicKey == null || publicKey.length == 0) {
            return null;
        }

        if (data == null || data.length == 0) {
            return null;
        }

        byte[] source = new byte[data.length];
        System.arraycopy(data, 0, source, 0, data.length);

        Cipher cipher = new Cipher();
        SM2 sm2 = SM2.Instance();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);

        ECPoint c1 = cipher.Init_enc(sm2, userKey);
        cipher.Encrypt(source);
        byte[] c3 = new byte[32];
        cipher.Dofinal(c3);

        //C1 C2 C3拼装成加密字串
        return Util.byteToHex(c1.getEncoded()) + Util.byteToHex(source) + Util.byteToHex(c3);
    }

    //数据解密
    public static byte[] decrypt(byte[] privateKey, byte[] encryptedData) throws IOException {
        if (privateKey == null || privateKey.length == 0) {
            return null;
        }

        if (encryptedData == null || encryptedData.length == 0) {
            return null;
        }
        //加密字节数组转换为十六进制的字符串 长度变为encryptedData.length * 2
        String data = Util.byteToHex(encryptedData);
        /***分解加密字串
         * （C1 = C1标志位2位 + C1实体部分128位 = 130）
         * （C3 = C3实体部分64位  = 64）
         * （C2 = encryptedData.length * 2 - C1长度  - C2长度）
         */
        byte[] c1Bytes = Util.hexToByte(data.substring(0, 130));
        int c2Len = encryptedData.length - 97;
        byte[] c2 = Util.hexToByte(data.substring(130, 130 + 2 * c2Len));
        byte[] c3 = Util.hexToByte(data.substring(130 + 2 * c2Len, 194 + 2 * c2Len));

        SM2 sm2 = SM2.Instance();
        BigInteger userD = new BigInteger(1, privateKey);

        //通过C1实体字节来生成ECPoint
        ECPoint c1 = sm2.ecc_curve.decodePoint(c1Bytes);
        Cipher cipher = new Cipher();
        cipher.Init_dec(userD, c1);
        cipher.Decrypt(c2);
        cipher.Dofinal(c3);

        //返回解密结果
        return c2;
    }


    public static byte[] sign(byte[] userId, byte[] privateKey, byte[] sourceData) throws IOException {
        if (privateKey == null || privateKey.length == 0) {
            return null;
        }

        if (sourceData == null || sourceData.length == 0) {
            return null;
        }

        SM2 sm2 = SM2.Instance();
        BigInteger userD = new BigInteger(privateKey);

        ECPoint userKey = sm2.ecc_point_g.multiply(userD);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);

        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);

        SM2Result sm2Result = new SM2Result();
        sm2.sm2Sign(md, userD, userKey, sm2Result);

        DERInteger d_r = new DERInteger(sm2Result.getR());
        DERInteger d_s = new DERInteger(sm2Result.getS());
        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(d_r);
        v2.add(d_s);
        DERObject sign = new DERSequence(v2);
        return sign.getDEREncoded();
    }

    public static boolean verifySign(byte[] userId, byte[] publicKey, byte[] sourceData, byte[] signData) throws IOException {
        if (publicKey == null || publicKey.length == 0) {
            return false;
        }

        if (sourceData == null || sourceData.length == 0) {
            return false;
        }

        SM2 sm2 = SM2.Instance();
        ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);

        SM3Digest sm3 = new SM3Digest();
        byte[] z = sm2.sm2GetZ(userId, userKey);
        sm3.update(z, 0, z.length);
        sm3.update(sourceData, 0, sourceData.length);
        byte[] md = new byte[32];
        sm3.doFinal(md, 0);

        ByteArrayInputStream bis = new ByteArrayInputStream(signData);
        ASN1InputStream dis = new ASN1InputStream(bis);
        DERObject derObj = dis.readObject();
        Enumeration<DERInteger> e = ((ASN1Sequence) derObj).getObjects();
        BigInteger r = ((DERInteger) e.nextElement()).getValue();
        BigInteger s = ((DERInteger) e.nextElement()).getValue();
        SM2Result sm2Result = new SM2Result();
        sm2Result.setR(r);
        sm2Result.setS(s);

        sm2.sm2Verify(md, userKey, sm2Result.getR(), sm2Result.getS(), sm2Result);
        return sm2Result.getR().equals(sm2Result.getR2());
    }

    /**
     * 解密前端密码
     *
     * @param pCode 前端密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String decryptWeb(String pCode) throws Exception {
        return new String(SM2Utils.decrypt(Util.hexToByte(PRI_KEY), Util.hexToByte("04" + pCode)));
    }

    /**
     * 解密后端密码
     *
     * @param pCode 后端密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String decryptDB(String pCode) throws Exception {
        return new String(SM2Utils.decrypt(Util.hexToByte(PRI_KEY), Util.hexToByte(pCode)));
    }

    /**
     * 加密
     *
     * @param pCode 密码
     * @return 密码
     * @throws Exception 异常
     */
    public static String encryptDB(String pCode) throws Exception {
        return SM2Utils.encrypt(Util.hexStringToBytes(PUB_KEY), pCode.getBytes());
    }

    public static void main(String[] args) throws Exception {

//        String webPCode = "5a42a57a571a414c494ace5ffb97f8c880173f98a71abb05d5de6a82c87848126a1a47fb7ad81aad8c497dc526f44a54f76a4102abcb47970d1192c911995f56c518a5c3e2eee4dfb8fb136b30f22aa36564ac46062a489c9188f87a6c2658e45e2082f967184f06140d";
//        String dbPCode = "04823CAFFAC4EB11421C38854473CCD1BAABFDB261D7752C8C3065F619C581F25F540E438B8212B1F59AB22EDEC3861719CA3A1BF6DC40F7625CBA09E40892D7491244662AB0D9D60BD05DEC8B36ECB07C66B6BD190EFD42BEA2B05A6097BDBD1C49B954CE04E2EF3A8DD3";
//
//        System.out.println("前端解密：" + new String(SM2Utils.decrypt(Util.hexToByte(privateKey), Util.hexToByte("04" + webPCode))));
//        System.out.println("后端解密：" + new String(SM2Utils.decrypt(Util.hexToByte(privateKey), Util.hexToByte(dbPCode))));
//
//        String pCodeEncryption = SM2Utils.encrypt(Util.hexStringToBytes(publicKey), "central123".getBytes());
//        System.out.println("加密：" + pCodeEncryption);

//        String privateKey = "4F9ACC3A24CB9557754ABA7ED0D6F1CC849AEFBEE7C9197F5E569A2D4AC792D5";
//        String publicKey = "04D012B3DA830A808D0733D1BE231EC8F48FDB13FCC09B7678769F0BF68262EE79922140C786C5E02283AC78D3198F2BFB2A07E4B1949A627BEB0950142FD191B3";
//        //测试验签
//        String plainText = "{\"startIndex\":1,\"pageSize\":17,\"markName\":\"\",\"detailLog\":\"查询条件：所有标记\"}";
//        String userId = "1234567812345678";
//        byte[] signBytes = SM2Utils.sign(userId.getBytes(), Util.hexToByte(privateKey), plainText.getBytes());
//        System.out.println("sign: " + Util.getHexString(signBytes));
//        boolean vs = SM2Utils.verifySign(userId.getBytes(), Util.hexToByte(publicKey), plainText.getBytes(), signBytes);
//          自转方法
//        boolean vs = SM2Utils.verifySign(userId.getBytes(), ByteUtil.toByteArray(publicKey), plainText.getBytes(), siginData.getBytes());
//        System.out.println("签名验证结果 - " + vs);

        //测试验签2
        String publicKey = "0461fb6367aefc6db728b8bd889349c25fac42c94a78c9d564af02feba1613d9cbb5f6a62151941873e5b2428033413ab7502b25dfde03c51bdcc4fb3027cb3bd0";
        String signStr = "MEQCIEOKhqkzH7h+nCCHX2TnwJIT1Keg+JyEH/7HWMlqArt4AiB88/sYlI4t+1iWYfv33LxhyGusOBFXVI5qKB8Hy7Tp5w==";
        String plainText = "12345678123456781234567812345678";
        String userId = "1234567812345678";

        byte[] signBytes = Base64.decode(signStr);
        boolean vs = SM2Utils.verifySign(userId.getBytes(), Util.hexToByte(publicKey), plainText.getBytes(), signBytes);
        System.out.println("签名验证结果 - " + vs);

    }
}
