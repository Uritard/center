package com.yjh.accessudp.netty.server;

import com.yjh.accessudp.common.Constant;
import com.yjh.accessudp.common.utils.ByteUtil;
import com.yjh.accessudp.common.utils.MeteValueUtils;
import com.yjh.accessudp.commons.logs.SpringBeanUtils;
import com.yjh.accessudp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessudp.module.device.entity.SYAllInfo;
import com.yjh.accessudp.module.device.entity.TCfgDataCurrent;
import com.yjh.accessudp.module.device.service.TCfgMeteService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private RedisTemplate redisTemplate;
    private TCfgMeteService tCfgMeteService;
    private String UNION_URL;
    private String SEQUENCE_URL;
    public UDPServerHandler(RedisTemplate redisTemplate,TCfgMeteService tCfgMeteService,String UNION_URL,String SEQUENCE_URL) {
        this.redisTemplate = redisTemplate;
        this.tCfgMeteService = tCfgMeteService;
        this.UNION_URL=UNION_URL;
        this.SEQUENCE_URL =SEQUENCE_URL;
    }
    private boolean isThreadStart = true;
    public boolean getIsThreadStart() { return isThreadStart; }

    //场站号
    private String strChannelID = "TT";

    private byte[] bufBytes = new byte[1024];
    private int bufdateLen;
    private ChannelHandlerContext ctx;


    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        Channel channel = ctx.channel();
        if (channel.id() != null) {
            log.info("id: " + channel.id() + ", strChannelID: " + strChannelID + " left," + "Onlinesize: ");
        }
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("clientDisconnect: " + e.getMessage());
        }
        log.info("mapsAfterRemoved: ");
        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strChannelID == null || strChannelID.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            strChannelID = strChannelID.replace("\0", "");
            Object strid2 = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strChannelID)).get("deviceId");
            if (strid2 != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
                log.info("offlineClientId:" + strid2 + "  channel.isActive(): " + channel.isActive());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        this.ctx = ctx;
        log.info("mapsAfterAdded: ");
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: ");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if (cause.toString().equals("java.io.IOException: 远程主机强迫关闭了一个现有的连接。") || cause.toString().equals("java.io.IOException: Connection reset by peer")) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }

    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf byteBuf = (ByteBuf) msg;
//        byte[] bytes = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(bytes);
//        log.info("bytes: "+new String(bytes).replace("\0", ""));
//        log.info("online.. " + "remoteAddress: " + ctx.channel().remoteAddress());
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
//        DataDealThread dataDealThread = new DataDealThread(this, true);
//        Thread thread = new Thread(dataDealThread);
//        thread.setDaemon(true);
//        thread.start();
//        ReferenceCountUtil.release(byteBuf);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // 解析数据包
        ByteBuf sss = datagramPacket.content();
        byte[] req = new byte[sss.readableBytes()];
        sss.readBytes(req);
        int ss = ByteUtil.bytes2Int(req);
        log.info("ss: "+ss);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : req) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSend:" + Str + " : " + strChannelID);

        //解析 Str = eb 90 eb 90 55 01 00 0a 27 01 06 e5 8a a8 e4 bd 9c 06 e5 90 88 e4 bd 8d
        String[] udp = Str.toString().split(" ");
        // 处理校验和
        if(udp.length > 6){
            Integer he = 0;
            for (int i = 5; i < udp.length-1; i++) {
                he = he + Integer.parseInt(udp[i], 16);
            }
            Integer check = Integer.parseInt(udp[udp.length-1],16);
            if(check != (he%256)){
                //校验和不对
                log.info("计算校验和{}，报文校验和{}",he,check);
                return;
            }
        }
        if("55".equals(udp[4])){
            //获取meteid
            Integer meteId = Integer.valueOf(new BigInteger(udp[8]+udp[7],16).toString());
            log.info("meteId:   "+meteId);
            //获取meteKind
            Integer meteKind = Integer.valueOf(new BigInteger(udp[9],16).toString());
            if(meteKind == 0){
                meteKind = 3;
            }
            log.info("meteKind:  "+meteKind);
            //获取属性长度
            Integer valueLength = Integer.valueOf(new BigInteger(udp[10],16).toString());
            log.info("valueLength:  "+valueLength);
            //获取属性
            String value = arrayToString(udp,11,valueLength,true);
            log.info("属性:  " + value);
            //获取描述长度
            int weizhi = 10+valueLength+1;
            Integer commitLength = Integer.valueOf(new BigInteger(udp[weizhi],16).toString());
            log.info("commitLength:  "+commitLength);
            //获取描述
            String commit = arrayToString(udp,weizhi+1,commitLength,true);
            log.info("描述:  " + commit);
//            String commitValue = MeteValueUtils.meteValues(commit);
//            log.info("描述数字:"+commitValue);
            //获取时间
            String shijianchuo = arrayToString(udp,weizhi+1+commitLength,7,false);
//            getCP56time2a
//            String timea="20"+Integer.valueOf(new BigInteger(udp[weizhi+1+commitLength+7],16).toString())+"-"+Integer.valueOf(new BigInteger(udp[weizhi+1+commitLength+6],16).toString())
//                    +"-"+Integer.valueOf(new BigInteger(udp[weizhi+1+commitLength+7],16).toString());
//            long day= Long.valueOf(shijianchuo,16);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
            String time = TimeScale(shijianchuo);
            //String time2 =TimeScale(shijianchuo);
            log.info("time:  "+time);

            TCfgDataCurrent tCfgDataCurrent = tCfgMeteService.selectByPrimaryIdTCfgDataCurrent(meteId.toString());
            if(tCfgDataCurrent == null){
                tCfgDataCurrent = new TCfgDataCurrent();
                tCfgDataCurrent.setMeteId(Long.valueOf(meteId));
                tCfgDataCurrent.setDeviceId(Long.valueOf(tCfgMeteService.selectByMeteId(meteId.toString())));
                tCfgDataCurrent.setMeteKind(meteKind);
                //tCfgDataCurrent.setRecordTime(sdf.parse(time));
                tCfgDataCurrent.setRecordTime(new Date());
                tCfgDataCurrent.setMeteComment(value);
                tCfgDataCurrent.setMeteValue(commit);
                tCfgMeteService.insertTCfgDataCurrent(tCfgDataCurrent);
            }else {
                //库里已经有数据了
                tCfgDataCurrent.setLastMeteValue(tCfgDataCurrent.getMeteValue());
                tCfgDataCurrent.setRecordTime(sdf.parse(time));
                tCfgDataCurrent.setMeteComment(value);
                tCfgDataCurrent.setMeteValue(commit);
                tCfgMeteService.updateTCfgDataCurrent(tCfgDataCurrent);
            }
//            Map<String, String> map = new HashMap<>();
//            map.put("meteId",meteId.toString());
//            union(map);
            //将实时表里的数据更新到历史表里
            tCfgMeteService.insertIntoHis(tCfgDataCurrent);

            {//遥控信号
                if(meteKind == 3){
                    getUrl(SEQUENCE_URL,meteId.toString());
                    getUrl(UNION_URL,meteId.toString());
                }else {
                    getUrl(UNION_URL,meteId.toString());
                }

            }

        }else if ("43".equals(udp[4])){
            Integer doesHas = Integer.valueOf(new BigInteger(udp[7],16).toString());
            log.info("有无后续：  "+doesHas);
            if(doesHas == 1){
                Integer xuHao = Integer.valueOf(new BigInteger(udp[8],16).toString());
                log.info("帧序号：  "+xuHao);
                //其实传输位置 9-12
                Integer valueLength = Integer.valueOf(new BigInteger(udp[13],16).toString());
                List<String> listByte = new ArrayList<>();
                for(int i =0;i<valueLength;i++){
                    listByte.add(udp[14+i]);
                    Constant.listAllByte.add(udp[14+i]);
                }
                String weiZhi= arrayToString(udp,9,4,true);
                //String neiRong= arrayToString(udp,14,valueLength,true);
                log.info("位置："+weiZhi);
                Constant.data.put(xuHao,listByte);
            }
            if(doesHas == 0){
                Integer xuHao = Integer.valueOf(new BigInteger(udp[8],16).toString());
                log.info("帧序号：  "+xuHao);
                //其实传输位置 9-12
                Integer valueLength = Integer.valueOf(new BigInteger(udp[13],16).toString());
                List<String> listByte = new ArrayList<>();
                for(int i =0;i<valueLength;i++){
                    listByte.add(udp[14+i]);
                    Constant.listAllByte.add(udp[14+i]);
                }
                Constant.data.put(xuHao,listByte);

                List<String> listForSortByte =new ArrayList<>();
                for(int i=0;i<Constant.data.size();i++ ){
                    Constant.data.get(i);
                    for (String item:Constant.data.get(i)) {
                        listForSortByte.add(item);
                    }

                }
                String data="";
                if(Constant.sort){
                    data = arrayToString(listForSortByte);
                    data = toStringHex(data);
                    log.info("文件内容map： "+data);
                }else {
                    data = arrayToString(Constant.listAllByte);
                    data = toStringHex(data);
                    log.info("文件内容： "+data);
                }
                String regex ="\\#.*?(是|不是)";
                Matcher matcher = Pattern.compile(regex).matcher(data);
                List<SYAllInfo> list = new LinkedList<>();
                while (matcher.find()){
                    String str = matcher.group();
                    str = str.replaceAll("\\t"," ");

                    String[]strArray = str.split("\\s+");
                    if("".equals(strArray[0])){
                        strArray= Arrays.copyOfRange(strArray,1,strArray.length);
                    }
                    //取数据
                    SYAllInfo syAllInfo = new SYAllInfo();
                    syAllInfo.setStationId(strArray[1]);
                    String[] mete = strArray[3].split("/");
                    String meteName = mete[mete.length-2]+"/"+mete[mete.length-1]+"-"+strArray[4];
                    syAllInfo.setMeteId(strArray[2]);
                    syAllInfo.setMeteName(meteName);
                    syAllInfo.setDeviceId(strArray[2]);
                    syAllInfo.setDeviceName(strArray[3]);
                    Integer meteKind = strArray[4].contains("遥信")?1:(strArray[4].contains("遥测")?2:(strArray[4].contains("遥控")?3:(strArray[4].contains("遥调")?4:5)));
                    syAllInfo.setMeteKind(meteKind);
                    syAllInfo.setMeteCode(strArray[5]);
                    list.add(syAllInfo);
                    //list.add(str);
                }
                tCfgMeteService.deleteAll();
                tCfgMeteService.insertForAll(list);
                Constant.dataByDevice = data;
                Constant.listAllByte = new ArrayList<>();
                Constant.data = new HashMap<>();
            }

        }

    }

    private  String getCP56time2a(String str) {
     return "20" + Integer.parseInt(str.substring(12, 14), 16) + "-" + Integer.parseInt(str.substring(10, 12), 16)
       + "-" + Integer.parseInt(str.substring(8, 10), 16) + " " + Integer.parseInt(str.substring(6, 8), 16)
             + ":" + Integer.parseInt(str.substring(4, 6), 16) + ":"
             + Integer.parseInt(str.substring(2, 4) + "" + str.substring(0, 2), 16) / 1000;
    }

    private  String TimeScale(String str) {
        StringBuilder result = new StringBuilder();

        int year = Integer.parseInt(str.substring(12, 14), 16) & 0x7F;
        int month = Integer.parseInt(str.substring(10, 12), 16) & 0x0F;
        int day = Integer.parseInt(str.substring(8, 10), 16) & 0x1F;
        int week = (Integer.parseInt(str.substring(8, 10), 16) & 0xE0) / 32;
        int hour = Integer.parseInt(str.substring(6, 8), 16) & 0x1F;
        int minute = Integer.parseInt(str.substring(4, 6), 16) & 0x3F;
        int second = (Integer.parseInt(str.substring(2, 4), 16) << 8) + Integer.parseInt(str.substring(0, 2), 16);

        result.append("20");
        result.append(year).append("-");
        result.append(String.format("%02d", month)).append("-");
        result.append(String.format("%02d", day)).append(" ");
        result.append(hour).append(":").append(minute).append(":");
        result.append(second / 1000 );

        return result.toString();
    }


    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, Constant.encoding);// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


    public String getUrl(String url, String json) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        String result = "";
        try {
            URI uri = new URIBuilder(url).setParameter("meteId", json).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Content-type", "application/json;charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {e.getMessage();}
        return result;
    }

    private void union(Map<String, String> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject("http://iot-center-platform/tCfgDataCurrent/v1/unionTest", map, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String arrayToString(List<String> udp)throws UnsupportedEncodingException{
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < udp.size();i++){
            stringBuilder.append(udp.get(i));
        }
        String str =  stringBuilder.toString();
//        if(flag){
//            return hexStr2Str(str);
//        }
        return str;
        //return hexToString(str);
    }

    public String arrayToString(String[] udp,int start,int length,boolean flag)throws UnsupportedEncodingException{
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < length;i++){
            stringBuilder.append(udp[start+i]);
        }
        String str =  stringBuilder.toString();
        if(flag){
            return toStringHex(str);
        }
        return str;
    }


    private void handleDate(String msgData) {
        //TODO
    }

    private void send(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSend:" + Str + " : " + strChannelID);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    public void ProcSend() {
        //            定时读取文件内容
        log.info("定时读取。。。");
    }

}
