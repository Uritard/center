package com.yjh.accessatmosphere.thread;

import com.yjh.accessatmosphere.common.Constant;
import com.yjh.accessatmosphere.commons.utils.weatherUtils.ParamConfig;
import com.yjh.accessatmosphere.commons.utils.weatherUtils.SerialPortUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lqh
 * @since 2020/12/31
 */
public class ListerThread extends Thread{
    private ParamConfig paramConfig;

    public ListerThread(ParamConfig paramConfig) {
        this.paramConfig=paramConfig;
    }

    @Override
    public void run() {
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        // 实例化串口操作类对象
        SerialPortUtils serialPort = new SerialPortUtils();
        // 创建串口必要参数接收类并赋值，赋值串口号，波特率，校验位，数据位，停止位
        //ParamConfig paramConfig = new ParamConfig("COM3", 19200, 0, 8, 1);
        // 初始化设置,打开串口，开始监听读取串口数据
        serialPort.init(paramConfig);
        Constant.serialPort = serialPort;
        // 调用串口操作类的sendComm方法发送数据到串口
        //serialPort.sendComm(date);
        //serialPort.readComm();
        //serialPort.serialEvent();
        // 关闭串口
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        serialPort.closeSerialPort();
    }

}
