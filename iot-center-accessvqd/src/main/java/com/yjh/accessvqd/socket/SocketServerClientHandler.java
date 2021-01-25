package com.yjh.accessvqd.socket;


import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.InputStream;
import java.net.Socket;

/**
 * @program: com.io.socket.server
 * @description: 服务端客户消息处理线程类
 * @author: liujinghui
 * @create: 2019-02-16 11:02
 **/
public class SocketServerClientHandler extends Thread {

    //每个消息通过Socket进行传输
    private Socket clientConnectSocket;
    private ChanResultService chanResultService;
    private TDiagnosePlanDao tDiagnosePlanDao;
    private RedisTemplate redisTemplate;

    public SocketServerClientHandler(Socket clientConnectSocket,ChanResultService chanResultService,TDiagnosePlanDao tDiagnosePlanDao,RedisTemplate redisTemplate) {
        this.clientConnectSocket = clientConnectSocket;
        this.chanResultService=chanResultService;
        this.tDiagnosePlanDao=tDiagnosePlanDao;
        this.redisTemplate=redisTemplate;
    }

    @Override
    public void run() {
        String result="";
        try {
            InputStream inputStream = clientConnectSocket.getInputStream();
            while (true) {
                byte[] data = new byte[100];
                int len;
                while ((len = inputStream.read(data)) != -1) {
                    String message = new String(data, 0, len);
                    System.out.println("客户端传来消息: " + message);
//                    clientConnectSocket.getOutputStream().write(data);
                    result=result+message;
                    // TODO: 2021/1/21 留意多点诊断结果
                    if(result.contains("</ChanResult>")){
                        PacketDataHandler packetDataHandler=new PacketDataHandler(chanResultService,tDiagnosePlanDao,redisTemplate);
                        packetDataHandler.analysisChanResult(result);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
