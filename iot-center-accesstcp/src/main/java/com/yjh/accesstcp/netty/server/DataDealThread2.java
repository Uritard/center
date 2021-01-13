package com.yjh.accesstcp.netty.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

@lombok.extern.slf4j.Slf4j
public class DataDealThread2 implements Runnable {

    private volatile boolean isThreadStart;

    public DataDealThread2(boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
    }

    @Override
    public void run() {
        try {
            // 定义一个接收端，并且指定了接收的端口号
            DatagramSocket socket = new DatagramSocket(9300);
            while (isThreadStart) {
                byte[] buf = new byte[1024];
                // 解析数据包
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String ip = packet.getAddress().getHostAddress();
                log.info("ip: "+ip);
                buf = packet.getData();
                String data = new String(buf, 0, packet.getLength());
                System.out.println("收到 " + ip + " 发来的消息：" + data);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
