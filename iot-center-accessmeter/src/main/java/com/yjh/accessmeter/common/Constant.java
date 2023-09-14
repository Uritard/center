package com.yjh.accessmeter.common;

import com.yjh.accessmeter.module.device.entity.TMeter;
import io.netty.util.AttributeKey;

import java.util.ArrayList;
import java.util.List;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
public class Constant {
    /**
     * 帧前导符
     */
    public static  final byte FRAME_PREAMBLE = (byte) 0xfe;
    /**
     * 帧起始符
     */
    public static   final byte START_OF_FRAME =0x68;

    public static   final byte DIFF_VALUE =0x33;
    /**
     * 帧结束符
     */
    public static   final byte END_OF_FRAME = 0x16;
    /**
     * 从站正常应答控制码
     */
    public  static final  byte CONTROLL_CODE_ANSWER = (byte) 0x81;

    /**
     * 主站读数据控制码
     */
    public  static final  byte CONTROLL_CODE_REQUEST =  0x01;

    /**
     * 正向有功总电能数据类型
     */
    public static final byte[] DATA_TYPE_POSITVICE_POWER_TOTAL = new byte[]{(byte) 0x10, (byte) 0x90};
    /**
     * 正向无功 positive_reactive
     */
    public static final byte[] DATA_TYPE_POSITIVE_REACTIVE_POWER_TOTAL = new byte[]{(byte) 0x10, (byte) 0x91};
    /**
     * 反向无功 negative_reactive
     */
    public static final byte[] DATA_TYPE_NEGATIVE_REACTIVE_POWER_TOTAL = new byte[]{(byte) 0x20, (byte) 0x91};

    public static final int minLength = 12;
    public static final AttributeKey<TMeter> tMeterAttributeKey =AttributeKey.valueOf("TMeter");

    /**
     * 数据标识数组
     */
    private static List<byte[]> DATAIDENTITY = new ArrayList<byte[]>(){{
        add(new byte[]{(byte)0x10,(byte)0x90});//当前正向有功总电能
        add(new byte[]{(byte)0x20,(byte)0x90});//当前反向有功总电能
        add(new byte[]{(byte)0x10,(byte)0x91});//当前正向无功总电能
        add(new byte[]{(byte)0x20,(byte)0x91});//当前反向无功总电能
        add(new byte[]{(byte)0x10,(byte)0x94});//上月正向有功总电能
        add(new byte[]{(byte)0x20,(byte)0x94});//上月反向有功总电能
        add(new byte[]{(byte)0x10,(byte)0x95});//上月正向无功总电能
        add(new byte[]{(byte)0x20,(byte)0x95});//上月反向无功总电能
        add(new byte[]{(byte)0x10,(byte)0x98});//上上月正向有功总电能
        add(new byte[]{(byte)0x20,(byte)0x98});//上上月反向有功总电能
        add(new byte[]{(byte)0x10,(byte)0x99});//上上月正向无功总电能
        add(new byte[]{(byte)0x20,(byte)0x99});//上上月反向无功总电能
        add(new byte[]{(byte)0x11,(byte)0xB6});//A相电压
        add(new byte[]{(byte)0x12,(byte)0xB6});//B相电压
        add(new byte[]{(byte)0x13,(byte)0xB6});//C相电压
        add(new byte[]{(byte)0x21,(byte)0xB6});//A相电流
        add(new byte[]{(byte)0x22,(byte)0xB6});//B相电流
        add(new byte[]{(byte)0x23,(byte)0xB6});//C相电流
        add(new byte[]{(byte)0x30,(byte)0xB6});//瞬时总功率
        add(new byte[]{(byte)0x31,(byte)0xB6});//瞬时A相有功功率
        add(new byte[]{(byte)0x32,(byte)0xB6});//瞬时B相有功功率
        add(new byte[]{(byte)0x33,(byte)0xB6});//瞬时C相有功功率
        add(new byte[]{(byte)0x40,(byte)0xB6});//瞬时总无功功率
        add(new byte[]{(byte)0x41,(byte)0xB6});//瞬时A相总无功功率
        add(new byte[]{(byte)0x42,(byte)0xB6});//瞬时B相总无功功率
        add(new byte[]{(byte)0x43,(byte)0xB6});//瞬时C相总无功功率
        add(new byte[]{(byte)0x50,(byte)0xB6});//总功率因数
        add(new byte[]{(byte)0x51,(byte)0xB6});//A相功率因数
        add(new byte[]{(byte)0x52,(byte)0xB6});//B相功率因数
        add(new byte[]{(byte)0x53,(byte)0xB6});//C相功率因数
        //增加重复项
        add(new byte[]{(byte)0x30,(byte)0xB6});//瞬时总功率
        add(new byte[]{(byte)0x31,(byte)0xB6});//瞬时A相有功功率
        add(new byte[]{(byte)0x32,(byte)0xB6});//瞬时B相有功功率
        add(new byte[]{(byte)0x33,(byte)0xB6});//瞬时C相有功功率
        add(new byte[]{(byte)0x40,(byte)0xB6});//瞬时总无功功率
    }};
}
