package com.yjh.gateway.commons.utils.gmhelper;

import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SM2Verify_SKF {
	
	//ECC公钥交换数据块

	/**
	 * 单独传入R和S进行验签
	 * @param pubKeyData 公钥数据
	 * @param R R数据
	 * @param S S数据
	 * @param data 签名原文
	 * @param userID 用户ID
	 * @return
	 */
	public static boolean SM2VerifyRS(String pubKeyData,byte[] R,byte[] S,String data,String userID) {
    	byte[] pubKeyDataByte=Base64.decode(pubKeyData);
    	//String pubKeyHex=Hex.toHexString(pubKeyDataByte);
    	//System.out.println("公钥:");
    	//System.out.println(pubKeyHex);
    	//取出公钥的X
    	byte[] x=new byte[32];
    	System.arraycopy(pubKeyDataByte, 36, x, 0, x.length);
    	String xHex=Hex.toHexString(x);
    	//System.out.println("X:");
    	//System.out.println(xHex);
    	//取出公钥的Y
    	byte[] y=new byte[32];
    	System.arraycopy(pubKeyDataByte, 100, y, 0, y.length);
    	String yHex=Hex.toHexString(y);
    	//System.out.println("Y:");
    	//System.out.println(yHex);
    	//进行拼装
        byte[] rsData =new byte[64];
        System.arraycopy(R, 0, rsData, 0, 32);
        System.arraycopy(S, 0, rsData, 32, 32);
        //System.out.println("R和S拼装后的数据:");
        //System.out.println(Hex.toHexString(rsData));
        
        ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
        boolean Rtn=false;
        try {
			 Rtn= SM2Util.verify(pubKey,userID.getBytes(), data.getBytes(), SM2Util.encodeSM2SignToDER(rsData));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return Rtn;
    }
	
	public static boolean SM2VerifyRS(String X,String Y,byte[] R,byte[] S,String data,String userID) {
		byte[] xByte=hexToByteArray(X);
    	String xHex=Hex.toHexString(xByte);
    	byte[] yByte=hexToByteArray(Y);
    	String yHex=Hex.toHexString(yByte);
		
    	//进行拼装
        byte[] rsData =new byte[64];
        System.arraycopy(R, 0, rsData, 0, 32);
        System.arraycopy(S, 0, rsData, 32, 32);
        //System.out.println("R和S拼装后的数据:");
        //System.out.println(Hex.toHexString(rsData));
        
        ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
        boolean Rtn=false;
        try {
			 Rtn= SM2Util.verify(pubKey,userID.getBytes(), data.getBytes(), SM2Util.encodeSM2SignToDER(rsData));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return Rtn;
    }
	
	/**
	 * 直接传入BASE64后的 Struct_ECCPUBLICKEYBLOB 公钥的结构体
	 * @param pubKeyData 符合Struct_ECCPUBLICKEYBLOB 公钥的结构体，BASE64数据
	 * @param signed 龙脉超级插件的签名值
	 * @param data 签名原文
	 * @param userID 用户ID
	 * @return
	 */
	public static boolean SM2Verify(String pubKeyData,String signed,String data,String userID) {
    	byte[] pubKeyDataByte=Base64.decode(pubKeyData);
    	//String pubKeyHex=Hex.toHexString(pubKeyDataByte);
    	//System.out.println("公钥:");
    	//System.out.println(pubKeyHex);
    	//取出公钥的X
    	byte[] x=new byte[32];
    	System.arraycopy(pubKeyDataByte, 36, x, 0, x.length);
    	String xHex=Hex.toHexString(x);
    	//System.out.println("X:");
    	//System.out.println(xHex);
    	//取出公钥的Y
    	byte[] y=new byte[32];
    	System.arraycopy(pubKeyDataByte, 100, y, 0, y.length);
    	String yHex=Hex.toHexString(y);
    	//System.out.println("Y:");
    	//System.out.println(yHex);
    	
    	byte[] signedByte=Base64.decode(signed);
    	//String signedHex=Hex.toHexString(signedByte);
    	//System.out.println("签名后的数据：");
    	//System.out.println(signedHex);
        ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
        return SM2Util.verify(pubKey,userID.getBytes(), data.getBytes(), signedByte);
    }
	
	/**
	 * 
	 * @param X 数据 16进制
	 * @param Y 数据 16进制
	 * @param signed 签名后的值
	 * @param data 签名原文
	 * @param userID 用户ID
	 * @return
	 */
	public static boolean SM2Verify(String X,String Y,String signed,String data,String userID) {
    	byte[] signedByte=Base64.decode(signed);
    	byte[] xByte=hexToByteArray(X);
    	String xHex=Hex.toHexString(xByte);
    	byte[] yByte=hexToByteArray(Y);
    	String yHex=Hex.toHexString(yByte);
    	//String signedHex=Hex.toHexString(signedByte);
    	//System.out.println("签名后的数据：");
    	//System.out.println(signedHex);
        ECPublicKeyParameters pubKey = BCECUtil.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
        return SM2Util.verify(pubKey,userID.getBytes(), data.getBytes(), signedByte);
    }
	
	/**
	 * 根据签名后的值，取出R
	 * @param signed 签名后的值
	 * @return R 数组
	 */
	public static byte[] R(String signed) {
		byte[] signedByte=Base64.decode(signed);
		//从ECC签名数据结构中取出R，判断第5位数据是否为0，如果是，则从第6位开始取值，长度取32；否则，则直接从第5位数据开始取值，长度取32字节。
    	byte[] r=new byte[32];
    	if(signedByte[4]==0) {
    		System.arraycopy(signedByte, 5, r, 0, r.length);
    	}else {
    		System.arraycopy(signedByte, 4, r, 0, r.length);
    	}
    	//String rHex=Hex.toHexString(r);
    	//System.out.println("签名后的数据的R:");
    	//System.out.println(rHex);
    	return r;
	} 
	/**
	 * 根据签名后的值取出S
	 * @param signed 签名后的值
	 * @return S 数组
	 */
	public static byte[] S(String signed) {
		byte[] signedByte=Base64.decode(signed);
		//从ECC签名数据结构中取出S，直接从结构最后面取32字节的S数据
    	byte[] s=new byte[32];
    	System.arraycopy(signedByte, signedByte.length-32, s, 0, s.length);
    	//String sHex=Hex.toHexString(s);
    	//System.out.println("签名后的数据的S:");
    	//System.out.println(sHex);
    	return s;
	} 
	
	/** 
	 * hex字符串转byte数组 
	 * @param inHex 待转换的Hex字符串 
	 * @return  转换后的byte数组结果 
	 */  
	public static byte[] hexToByteArray(String inHex){  
	    int hexlen = inHex.length();  
	    byte[] result;  
	    if (hexlen % 2 == 1){  
	        //奇数  
	        hexlen++;  
	        result = new byte[(hexlen/2)];  
	        inHex="0"+inHex;  
	    }else {  
	        //偶数  
	        result = new byte[(hexlen/2)];  
	    }  
	    int j=0;  
	    for (int i = 0; i < hexlen; i+=2){  
	        result[j]=hexToByte(inHex.substring(i,i+2));  
	        j++;  
	    }  
	    return result;   
	}  
	
	/** 
	 * Hex字符串转byte 
	 * @param inHex 待转换的Hex字符串 
	 * @return  转换后的byte 
	 */  
	public static byte hexToByte(String inHex){  
		return (byte)Integer.parseInt(inHex,16);  
	}


	public static void main(String[] args) throws UnsupportedEncodingException {
		String userId = "1234567812345678";
		String webcode = "4edb95a8fd78b7c674c6bb3c2dfaeaeb9f6dd15b6a8b4c7a738fba5100325c7b";
		String signStr = "MEYCIQD1KHjOtVM8gmdtF0bnT/jKuqvZB3xCzNf06Z96RDi27wIhAOtLNeZOi2jhk5KbwSk5m03DF80ZKa5PCARPXdTQOD5b";
		String sign64Str = "TUVZQ0lRRDFLSGpPdFZNOGdtZHRGMGJuVC9qS3VxdlpCM3hDek5mMDZaOTZSRGkyN3dJaEFPdExOZVpPaTJqaGs1S2J3U2s1bTAzREY4MFpLYTVQQ0FSUFhkVFFPRDVi";
		String pubkey = "AAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA5y2lr0dZBzqaEy1qhn60/uemjXWUyW0/4RT/BClM1d4AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHeH0SzmS3kElZnesFUHdoVErr+S0TglXcNbHk+E2EJf";

		String pubStr = Base64.toBase64String(pubkey.getBytes());
		System.out.println(pubStr);

		boolean st = SM2Verify(pubkey, signStr, webcode, userId);
		System.out.println(st);
	}
}
