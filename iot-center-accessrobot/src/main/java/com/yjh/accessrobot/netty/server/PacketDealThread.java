package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author YC
 * @date 2021/2/6 17:41
 */
@lombok.extern.slf4j.Slf4j
public class PacketDealThread implements Runnable {

    private int headNum;
    private String socketMessageHex;
    private RobotServerHandler robotServerHandler;

    public PacketDealThread(RobotServerHandler robotServerHandler,String socketMessageHex,int headNum){
        this.robotServerHandler = robotServerHandler;
        this.headNum = headNum;
        this.socketMessageHex = socketMessageHex;
    }


    @Override
    public void run() {
        try {
            chaibao(socketMessageHex,headNum);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }
    public  void chaibao(String socketMessageHex,int headNum) throws Exception{
        String onePacketString = null;
        String shengYuString = null;
        if(socketMessageHex.startsWith("eb90") && headNum >= 2) {
            //内容足够
            int limitNum = socketMessageHex.indexOf("eb90", socketMessageHex.indexOf("eb90") + 1) + 3;
            int limit = 0;
            if(socketMessageHex.length()-limitNum==1){
                log.info("一个完整的包，解析......");
                String body1 = socketMessageHex.substring(46,socketMessageHex.length()-4);
                byte[] onePacket = PlatformPacketUtil.HexString2Bytes(body1);
                StringBuilder Str2 = new StringBuilder();
                for (byte byteItem : onePacket) {
                    Str2.append(String.format("%02x ", byteItem));
                }
                log.info("准备解析的字节数组="+Str2);
                onePacketString = PlatformPacketUtil.toStringHex(body1);
                log.info("准备解析的xml=="+onePacketString);
//                robotServerHandler.stringToXml(onePacket,onePacketString);
            }else{
                log.info("大于一个完整的包");
                socketMessageHex = socketMessageHex.substring(0,limitNum+1);
                /*byte[] by = PlatformPacketUtil.HexString2Bytes(socketMessageHex);
                limit = PlatformPacketUtil.HexString2Bytes(socketMessageHex).length;*/
                shengYuString = onePacketString.replace(socketMessageHex,"");
                chaibao(shengYuString,appearNumber(shengYuString,"eb90"));
            }
        }else {
            if (socketMessageHex.startsWith("eb90")){
                String now = Constant.Packet + socketMessageHex;
                log.info("现在的包："+ now);
                chaibao(now,appearNumber(now,"eb90"));
            }else {
                String now = socketMessageHex + Constant.Packet;
                System.out.println("现在的包："+ now);
                chaibao(now,appearNumber(now,"eb90"));
            }
        }
    }
    /**
     * 获取指定字符串出现的次数
     *
     * @param srcText 源字符串
     * @param findText 要查找的字符串
     * @return
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }
}
