package com.yjh.accessudp.module.device.service;

import com.yjh.accessudp.module.device.dao.SysLogsDao;
import com.yjh.accessudp.module.device.entity.SysLogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
* @author tt
* @since 2020-08-12
*/
@Service
public class SysLogsService{

    @Autowired
    private SysLogsDao sysLogsDao;
    @Value("${spring.union.sendTo.ip}")
    private String SERVER_HOSTNAME;

    // 服务器端口
    @Value("${spring.send.server.port}")
    private String SERVER_PORT;
    // 本地发送端口
    @Value("${spring.send.local.port}")
     static String LOCAL_PORT;
    private String data;

    private Logger log = LoggerFactory.getLogger(SysLogsService.class);

    @Transactional(rollbackFor = Exception.class)
    public SysLogs selectByPrimaryId(String logId) {
        return this.sysLogsDao.selectByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public String sendFile(String devicePath)throws Exception {
        //读取联动设备的信息
        //TSysParam tSysParam = tCfgMeteService.selectByParamType("unionDeviceInfoPath");
        //String devicePath = "D:/code/qhTest/deviceInfo666.txt";
        log.info("文件路径：  "+devicePath);
        BufferedReader br = null;
        InputStreamReader in = null;
        String info = "";

        try  {
            in = new InputStreamReader(new FileInputStream(new File(devicePath)), "UTF-8");
            br = new BufferedReader(in);
            String line;
            String[] strArray = null;
            while ((line = br.readLine()) != null) {
                info = info+line;
            }

            br.close();
            in.close();
        } catch (IOException e) {
            log.info("读取文件错误: "+e);
        } finally {
            if(br != null ){
                br.close();
            }
            if(in != null ){
                in.close();
            }

        }
        try{
            this.data = info;
            Device(info);
            return info;
        }catch (Exception e){e.getMessage();}
        return "fail";
    }

    private int Device(String info) throws Exception {
        try {
            log.info("sss");
            DatagramSocket socket = new DatagramSocket(Integer.valueOf(LOCAL_PORT));
            udpForDevice(info,socket);
            log.info("InetAddress.getByName(SERVER_HOSTNAME): "+ InetAddress.getByName(SERVER_HOSTNAME));
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void udpForDevice(String info,DatagramSocket socket) throws Exception{
        int i = 0;

        List<Byte> list = new ArrayList<>();
        //头
        Integer a = 0xEB;
        Integer b = 0x90;
        Integer c = 0x43;
        Byte eb=a.byteValue();
        Byte o=b.byteValue();
        Byte f=c.byteValue();
        byte[] head ={eb,o,eb,o,f};//头 1-5
        for (byte item:head) {
            list.add(item);
        }

        //station
        Integer d = 0x01;
        Integer e = 0x00;
        byte station1 = d.byteValue();
        byte station2 = e.byteValue();
        byte[] station ={station1,station2};//站id 6-7
        for (byte item:station) {
            list.add(item);
        }

        String value = info;
        byte[] valueNeiRong = value.getBytes();//文件内容 15- 66
        Integer infoLength = valueNeiRong.length;
        int j;
        for( j=1 ;(j*240) < infoLength;j++){
            List<Byte> list2 = new ArrayList<>();
            list2.addAll(list);


            Integer h = 0x1;//后续 8
            list2.add(h.byteValue());

            Integer x = i;
            list2.add(x.byteValue());//序号 9

            //起始位置 10-13
            Integer shuai = i;
            list2.add(new Byte((byte)(shuai & 0xff)));
            list2.add(new Byte((byte)(shuai >> 8 & 0xff)));
            list2.add(new Byte((byte)(shuai >> 16 & 0xff)));
            list2.add(new Byte((byte)(shuai >> 24 & 0xff)));

            i++;

            Integer valueLength = 240;
            list2.add(valueLength.byteValue());//数据长度 14


            for(int cai = 0;cai<240;cai++){
                list2.add(valueNeiRong[cai+(j-1)*240]);//数据
            }
            //计算校验和
            Integer add = 0;
            for (int k = 5; k < list2.size(); k++) {
                add = add+list2.get(k);
            }
            list2.add(add.byteValue());
            byte[] uspRe = new byte[list2.size()];
            int zui = 0;
            for (byte item:list2) {
                uspRe[zui] = item;
                zui++;
            }
            DatagramPacket dp = new DatagramPacket(uspRe, uspRe.length, InetAddress.getByName(SERVER_HOSTNAME),
                    Integer.valueOf(SERVER_PORT));
            socket.send(dp);
            System.out.println(j);

        }
        if((j-1)*240 < valueNeiRong.length){//还有数据
            List<Byte> list2 = new ArrayList<>();
            list2.addAll(list);
            Integer h = 0x0;//后续 8
            list2.add(h.byteValue());

            Integer x = i;
            list2.add(x.byteValue());//序号 9

            //起始位置 10-13
            Integer shuai = i;
            list2.add(new Byte((byte)(shuai & 0xff)));
            list2.add(new Byte((byte)(shuai >> 8 & 0xff)));
            list2.add(new Byte((byte)(shuai >> 16 & 0xff)));
            list2.add(new Byte((byte)(shuai >> 24 & 0xff)));

            i++;

            Integer valueLength = valueNeiRong.length -(j-1)*240;
            list2.add(valueLength.byteValue());//数据长度 14

            for(int cai = 0;cai<valueLength;cai++){
                list2.add(valueNeiRong[(j-1)*240+cai]);//数据
            }
            //计算校验和
            Integer add = 0;
            for (int k = 5; k < list2.size(); k++) {
                add = add+list2.get(k);
            }
            list2.add(add.byteValue());
            byte[] uspRe = new byte[list2.size()];
            int zui = 0;
            for (byte item:list2) {
                uspRe[zui] = item;
                zui++;
            }
            DatagramPacket dp = new DatagramPacket(uspRe, uspRe.length, InetAddress.getByName(SERVER_HOSTNAME),
                    Integer.valueOf(SERVER_PORT));
            socket.send(dp);
        }
    }

}

