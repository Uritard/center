package com.yjh.accessrobot.common.smUtil;

import org.bouncycastle.asn1.*;
import org.bouncycastle.math.ec.ECPoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;

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
//	public static byte[] encrypt(byte[] publicKey, byte[] data) throws IOException {
//		if (publicKey == null || publicKey.length == 0) {
//			return null;
//		}
//
//		if (data == null || data.length == 0) {
//			return null;
//		}
//
//		byte[] source = new byte[data.length];
//		System.arraycopy(data, 0, source, 0, data.length);
//
//		Cipher cipher = new Cipher();
//		SM2 sm2 = SM2.Instance();
//		ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);
//
//		ECPoint c1 = cipher.Init_enc(sm2, userKey);
//		cipher.Encrypt(source);
//		byte[] c3 = new byte[32];
//		cipher.Dofinal(c3);
//
//		DERInteger x = new DERInteger(c1.getX().toBigInteger());
//		DERInteger y = new DERInteger(c1.getY().toBigInteger());
//		DEROctetString derDig = new DEROctetString(c3);
//		DEROctetString derEnc = new DEROctetString(source);
//		ASN1EncodableVector v = new ASN1EncodableVector();
//		v.add(x);
//		v.add(y);
//		v.add(derDig);
//		v.add(derEnc);
//		DERSequence seq = new DERSequence(v);
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DEROutputStream dos = new DEROutputStream(bos);
//		dos.writeObject(seq);
//		return bos.toByteArray();
//	}
//
//	public static byte[] decrypt(byte[] privateKey, byte[] encryptedData) throws IOException {
//		if (privateKey == null || privateKey.length == 0) {
//			return null;
//		}
//
//		if (encryptedData == null || encryptedData.length == 0) {
//			return null;
//		}
//
//		byte[] enc = new byte[encryptedData.length];
//		System.arraycopy(encryptedData, 0, enc, 0, encryptedData.length);
//
//		SM2 sm2 = SM2.Instance();
//		BigInteger userD = new BigInteger(1, privateKey);
//
//		ByteArrayInputStream bis = new ByteArrayInputStream(enc);
//		ASN1InputStream dis = new ASN1InputStream(bis);
//
//		DERObject derObj = dis.readObject();
//		ASN1Sequence asn1 = (ASN1Sequence) derObj;
//		DERInteger x = (DERInteger) asn1.getObjectAt(0);
//		DERInteger y = (DERInteger) asn1.getObjectAt(1);
//		ECPoint c1 = sm2.ecc_curve.createPoint(x.getValue(), y.getValue(), true);
//
//		Cipher cipher = new Cipher();
//		cipher.Init_dec(userD, c1);
//		DEROctetString data = (DEROctetString) asn1.getObjectAt(3);
//		enc = data.getOctets();
//		cipher.Decrypt(enc);
//		byte[] c3 = new byte[32];
//		cipher.Dofinal(c3);
//		return enc;
//	}
//
	public static byte[] sign(byte[] userId, byte[] privateKey, byte[] sourceData) throws IOException {
		if (privateKey == null || privateKey.length == 0) {
			return null;
		}
		
		if (sourceData == null || sourceData.length == 0) {
			return null;
		}
		
		SM2 sm2 = SM2.Instance();
		BigInteger userD = new BigInteger(privateKey);
		System.out.println("userD: " + userD.toString(16));
		System.out.println("");
		
		ECPoint userKey = sm2.ecc_point_g.multiply(userD);
//		System.out.println("椭圆曲线点X:" + userKey.getX().toBigInteger().toString(16));
//		System.out.println("椭圆曲线点Y:" + userKey.getY().toBigInteger().toString(16));
//		System.out.println("");
		
		SM3Digest sm3 = new SM3Digest();
		byte[] z = sm2.sm2GetZ(userId, userKey);
//		System.out.println("SM3摘要Z:" + Util.getHexString(z));
//	    System.out.println("");
	    
	    System.out.println("M: " + Util.getHexString(sourceData));
		System.out.println("");
		
		sm3.update(z, 0, z.length);
	    sm3.update(sourceData, 0, sourceData.length);
	    byte[] md = new byte[32];
	    sm3.doFinal(md, 0);
	    
//	    System.out.println("SM3摘要值:" + Util.getHexString(md));
//	    System.out.println("");
	    
	    SM2Result sm2Result = new SM2Result();
	    sm2.sm2Sign(md, userD, userKey, sm2Result);
//	    System.out.println("r: " + sm2Result.r.toString(16));
//	    System.out.println("s: " + sm2Result.s.toString(16));
//	    System.out.println("");
	    
	    DERInteger d_r = new DERInteger(sm2Result.r);
	    DERInteger d_s = new DERInteger(sm2Result.s);
	    ASN1EncodableVector v2 = new ASN1EncodableVector();
	    v2.add(d_r);
	    v2.add(d_s);
	    DERObject sign = new DERSequence(v2);
	    byte[] signdata = sign.getDEREncoded();
		return signdata;
	}
	
	@SuppressWarnings("unchecked")
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
//	    System.out.println("SM3摘要值:" + Util.getHexString(md));
//	    System.out.println("");
//
	    ByteArrayInputStream bis = new ByteArrayInputStream(signData);
	    ASN1InputStream dis = new ASN1InputStream(bis);
	    DERObject derObj = dis.readObject();
	    Enumeration<DERInteger> e = ((ASN1Sequence) derObj).getObjects();
	    BigInteger r = ((DERInteger)e.nextElement()).getValue();
	    BigInteger s = ((DERInteger)e.nextElement()).getValue();
	    SM2Result sm2Result = new SM2Result();
	    sm2Result.r = r;
	    sm2Result.s = s;

//	    System.out.println("r: " + sm2Result.r.toString(16));
//	    System.out.println("s: " + sm2Result.s.toString(16));
//	    System.out.println("");
	    
	    
	    sm2.sm2Verify(md, userKey, sm2Result.r, sm2Result.s, sm2Result);
        return sm2Result.r.equals(sm2Result.R);
	}
	
	public static void main(String[] args) throws Exception  {
//		String plainText = "message digest";
//		byte[] sourceData = plainText.getBytes();
//
//		// 国密规范测试私钥
//		String prik = "128B2FA8BD433C6C068C8D803DFF79792A519A55171B1B650C23661D15897263";
//		String prikS = new String(Base64.encode(Util.hexToByte(prik)));
////		System.out.println("prikS: " + prikS);
////		System.out.println("");
//
//		// ���ܹ淶�����û�ID
//		String userId = "ALICE123@YAHOO.COM";
//
//		System.out.println("ID: " + Util.getHexString(userId.getBytes()));
//		System.out.println("");
//
//		System.out.println("签名: ");
//		byte[] c = SM2Utils.sign(userId.getBytes(), Base64.decode(prikS.getBytes()), sourceData);
//		System.out.println("sign: " + Util.getHexString(c));
//		System.out.println("");
//
//		// 国密规范测试公钥
//		String pubk = "040AE4C7798AA0F119471BEE11825BE46202BB79E2A5844495E97C04FF4DF2548A7C0240F88F1CD4E16352A73C17B7F16F07353E53A176D684A9FE0C6BB798E857";
//		String pubkS = new String(Base64.encode(Util.hexToByte(pubk)));
//		System.out.println("pubkS: " + pubkS);
//		System.out.println("");
//
//
//		System.out.println("验签 ");
//		boolean vs = SM2Utils.verifySign(userId.getBytes(), Base64.decode(pubkS.getBytes()), sourceData, c);
//		System.out.println("验签结果： " + vs);
//		System.out.println("");
//
//		System.out.println("加密: ");
//		byte[] cipherText = SM2Utils.encrypt(Base64.decode(pubkS.getBytes()), sourceData);
//		System.out.println(new String(Base64.encode(cipherText)));
//		System.out.println("");
//
//		System.out.println("解密: ");
//		plainText = new String(SM2Utils.decrypt(Base64.decode(prikS.getBytes()), cipherText));
//		System.out.println(plainText);
		
	}
}
