package com.yjh.device.common.sm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.bouncycastle.util.encoders.Hex;

public class SM3Digest
{
	/** SM3值的长度 */
	private static final int BYTE_LENGTH = 32;

	/** SM3分组长度 */
	private static final int BLOCK_LENGTH = 64;

	/** 缓冲区长度 */
	private static final int BUFFER_LENGTH = BLOCK_LENGTH * 1;

	/** 缓冲区 */
	private byte[] xBuf = new byte[BUFFER_LENGTH];

	/** 缓冲区偏移量 */
	private int xBufOff;

	/** 初始向量 */
	private byte[] V = SM3.iv.clone();

	private int cntBlock = 0;

	public SM3Digest() {
	}

	public SM3Digest(SM3Digest t)
	{
		System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
		this.xBufOff = t.xBufOff;
		System.arraycopy(t.V, 0, this.V, 0, t.V.length);
	}

	/**
	 * SM3结果输出
	 *
	 * @param out 保存SM3结构的缓冲区
	 * @param outOff 缓冲区偏移量
	 * @return
	 */
	public int doFinal(byte[] out, int outOff)
	{
		byte[] tmp = doFinal();
		System.arraycopy(tmp, 0, out, 0, tmp.length);
		return BYTE_LENGTH;
	}

	public void reset()
	{
		xBufOff = 0;
		cntBlock = 0;
		V = SM3.iv.clone();
	}

	/**
	 * 明文输入
	 *
	 * @param in
	 *            明文输入缓冲区
	 * @param inOff
	 *            缓冲区偏移量
	 * @param len
	 *            明文长度
	 */
	public void update(byte[] in, int inOff, int len)
	{
		int partLen = BUFFER_LENGTH - xBufOff;
		int inputLen = len;
		int dPos = inOff;
		if (partLen < inputLen)
		{
			System.arraycopy(in, dPos, xBuf, xBufOff, partLen);
			inputLen -= partLen;
			dPos += partLen;
			doUpdate();
			while (inputLen > BUFFER_LENGTH)
			{
				System.arraycopy(in, dPos, xBuf, 0, BUFFER_LENGTH);
				inputLen -= BUFFER_LENGTH;
				dPos += BUFFER_LENGTH;
				doUpdate();
			}
		}

		System.arraycopy(in, dPos, xBuf, xBufOff, inputLen);
		xBufOff += inputLen;
	}

	private void doUpdate()
	{
		byte[] B = new byte[BLOCK_LENGTH];
		for (int i = 0; i < BUFFER_LENGTH; i += BLOCK_LENGTH)
		{
			System.arraycopy(xBuf, i, B, 0, B.length);
			doHash(B);
		}
		xBufOff = 0;
	}

	private void doHash(byte[] B)
	{
		byte[] tmp = SM3.CF(V, B);
		System.arraycopy(tmp, 0, V, 0, V.length);
		cntBlock++;
	}

	private byte[] doFinal()
	{
		byte[] B = new byte[BLOCK_LENGTH];
		byte[] buffer = new byte[xBufOff];
		System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
		byte[] tmp = SM3.padding(buffer, cntBlock);
		for (int i = 0; i < tmp.length; i += BLOCK_LENGTH)
		{
			System.arraycopy(tmp, i, B, 0, B.length);
			doHash(B);
		}
		return V;
	}

	public void update(byte in)
	{
		byte[] buffer = new byte[] { in };
		update(buffer, 0, 1);
	}

	public int getDigestSize()
	{
		return BYTE_LENGTH;
	}

	public static String sm3Encrypt(String str){
		return new String(sm3EncryptByte(str.getBytes()));
	}

	public static byte[] sm3EncryptByte(byte[] msg1){

		byte[] md = new byte[32];
		SM3Digest sm3 = new SM3Digest();
		sm3.update(msg1, 0, msg1.length);
		sm3.doFinal(md, 0);
		return Hex.encode(md);
	}

    public static void main(String[] args) {
//        System.out.println("sm3EncryptByte(): "+sm3Encrypt("shit"));

        JSONObject json = new JSONObject(true);
        json.put("user","xtgly");
        json.put("pass","d9b610cd9d5aeb67d3a2da321b8ab14bdbf7811f68857d54e41f2fbd068010afdaf37304d53526fcf066e2f7caacb24a4cc9c22da3ef792d56ed860c586649518790bf31f1452b6d68a551a850e4e9c679960aed451f6c21a740cc87fc6dae2b7d1afbc340c0d34c8a3e46");
        json.put("pbKey","048108703AD33E84CFD6F28F1FD920338F37195FE4807615DE451BE8C572CDBFC92C4AE7E640A9B01E48B5611FBCDFE848292CF1AB9634265E114138D9203A621F");
        String jsonStr = JSON.toJSONString(json, SerializeConfig.getGlobalInstance(),
                SerializerFeature.QuoteFieldNames);

        System.out.printf(jsonStr);
        String va = sm3Encrypt("xtgly");
        System.out.printf(va);
    }
}
