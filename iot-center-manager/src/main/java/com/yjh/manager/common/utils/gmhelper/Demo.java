/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.manager.common.utils.gmhelper;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * @Description: TODO(国密SM2签名验签 / SM3报文摘要)
 */
public class Demo {
    private static final Logger log = LoggerFactory.getLogger(Demo.class);
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

    public static String decryptIdentifier(String pCode, String priks) {
        try {
            if(StringUtils.isNoneBlank(pCode)  && !"null".equals(pCode) && StringUtils.isNoneBlank(priks) && !"null".equals(priks)){
                ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(new BigInteger(ByteUtils.fromHexString(priks)), SM2Util.DOMAIN_PARAMS);
                return new String(SM2Util.decrypt(SM2Engine.Mode.C1C2C3, priKey, ByteUtils.fromHexString("04" + pCode)));
            }
        } catch (IllegalArgumentException | InvalidCipherTextException e) {
            log.error("解密失败：{}", e.getMessage());
            log.error("解密参数， pCode: {}, priks: {}", pCode, priks);
        }
        return  pCode;
    }

    /**
     * 解密前端密码
     *
     * @param pCode 前端密码
     * @return 密码
     */
    public static String decrypt(String pCode) {
        if(StringUtils.isNoneBlank(pCode)){
            try {
                ECPrivateKeyParameters privateKeyParameters = BCECUtil.createECPrivateKeyParameters(prik, SM2Util.DOMAIN_PARAMS);
                return new String(SM2Util.decrypt(SM2Engine.Mode.C1C2C3, privateKeyParameters, ByteUtils.fromHexString("04" + pCode)));
            } catch (InvalidCipherTextException e) {
                log.error("解密失败", e);
                return "";
            }
        }else {
            return  "";
        }
    }

    public static void main(String[] args) {
        try {
            // String pCode = "0652e5f8b5fa8dc537255c522c1f12741e4fd86ae750f6580924fd4dcdaecba2b29e00d1ad665a0eb8515b07594fbe506396e5690bb797e81e5b133a11b04f8062306d2eb76d0fc46882547ef8e0b4217202e8807253a55449874393ea19323d575d9b742cca03";
            // String priks = "00815EF5AD16531BC50172EEAC863FC2F816EC7BF9F5A17971785402B20CB99801";
            //
            // String pubStr = decryptIdentifier(pCode, priks);
            // System.out.println(pubStr);
            String code = "6e110bdb2b95ca0bbd91cda74ac0aff974bbbb458c9b71fc25088d9fa2f6929242769863af85f465d5842bf44d060d7fe936b6f3e0084d478d05b0385d78095c2494fa8d93e53ffcaa7cb0a6082d190d0ce64a2e3af7d22d5d873b9a3c35dc0acc25e3c6be6085";
            String userName = Demo.decrypt(code);
            System.out.println(userName);

            ECPrivateKeyParameters privateKeyParameters = BCECUtil.createECPrivateKeyParameters(prik, SM2Util.DOMAIN_PARAMS);
            byte[] de = SM2Util.decrypt(SM2Engine.Mode.C1C2C3, privateKeyParameters, ByteUtils.fromHexString("04" + code));
            String decode = new String(de);
            System.out.println(decode);

        } catch (Exception e) {
            log.error("验签失败", e);
        }
    }
}
