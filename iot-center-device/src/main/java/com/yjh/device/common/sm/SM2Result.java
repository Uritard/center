package com.yjh.device.common.sm;

import java.math.BigInteger;

public class SM2Result
{
    public SM2Result() {
    }

    // 签名/验签
    private BigInteger r;
    private BigInteger s;
    private BigInteger R2;

    // 密钥交换
    private byte[] sa;
    private byte[] sb;
    private byte[] s1;
    private byte[] s2;

//    public ECPoint keyra;
//    public ECPoint keyrb;


    public BigInteger getR() {
        return r;
    }

    public void setR(BigInteger r) {
        this.r = r;
    }

    public byte[] getSa() {
        return sa;
    }

    public void setSa(byte[] sa) {
        this.sa = sa;
    }

    public byte[] getSb() {
        return sb;
    }

    public void setSb(byte[] sb) {
        this.sb = sb;
    }

    public byte[] getS1() {
        return s1;
    }

    public void setS1(byte[] s1) {
        this.s1 = s1;
    }

    public byte[] getS2() {
        return s2;
    }

    public void setS2(byte[] s2) {
        this.s2 = s2;
    }

    public BigInteger getS() {
        return s;
    }

    public void setS(BigInteger s) {
        this.s = s;
    }

    public BigInteger getR2() {
        return R2;
    }

    public void setR2(BigInteger r2) {
        R2 = r2;
    }
}
