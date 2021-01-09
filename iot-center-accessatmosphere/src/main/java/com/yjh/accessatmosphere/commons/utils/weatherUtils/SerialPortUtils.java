package com.yjh.accessatmosphere.commons.utils.weatherUtils;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessatmosphere.common.Constant;
import gnu.io.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

/**
 * @author lqh
 * @since 2020/12/30
 */
public class SerialPortUtils implements SerialPortEventListener {

    private static Logger logger = LoggerFactory.getLogger(SerialPortUtils.class);

    // 检测系统中可用的通讯端口类
    private CommPortIdentifier commPortId;
    // 枚举类型
    private Enumeration<CommPortIdentifier> portList;
    // RS232串口
    private SerialPort serialPort;
    // 输入流
    private InputStream inputStream;
    // 输出流
    private OutputStream outputStream;
    // 保存串口返回信息
    private String data ="";
    // 保存串口返回信息十六进制
    private String dataHex = "";

    /**
     * 初始化串口
     * @param: paramConfig  存放串口连接必要参数的对象
     * @return: void
     * @throws
     */
    @SuppressWarnings("unchecked")
    public void init(ParamConfig paramConfig) {
        // 获取系统中所有的通讯端口
        portList = CommPortIdentifier.getPortIdentifiers();
        // 记录是否含有指定串口
        boolean isExsist = false;
        // 循环通讯端口
        while (portList.hasMoreElements()) {
            commPortId = portList.nextElement();
            // 判断是否是串口
            if (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // 比较串口名称是否是指定串口
                if (paramConfig.getSerialNumber().equals(commPortId.getName())) {
                    // 串口存在
                    isExsist = true;
                    // 打开串口
                    try {
                        // open:（应用程序名【随意命名】，阻塞时等待的毫秒数）
                        serialPort = (SerialPort) commPortId.open(Object.class.getSimpleName(), 2000);
                        // 设置串口监听
                        serialPort.addEventListener(this);
                        // 设置串口数据时间有效(可监听)
                        serialPort.notifyOnDataAvailable(true);
                        // 设置串口通讯参数:波特率，数据位，停止位,校验方式
                        serialPort.setSerialPortParams(paramConfig.getBaudRate(), paramConfig.getDataBit(),
                                paramConfig.getStopBit(), paramConfig.getCheckoutBit());
                    } catch (PortInUseException e) {
                        logger.info("端口被占用");
                    } catch (TooManyListenersException e) {
                        logger.info("监听器过多");
                    } catch (UnsupportedCommOperationException e) {
                        logger.info("不支持的COMM端口操作异常");
                    }
                    // 结束循环
                    break;
                }
            }
        }
        // 若不存在该串口则抛出异常
        if (!isExsist) {
            logger.info("不存在该串口！");
        }
    }

    /**
     * 实现接口SerialPortEventListener中的方法 读取从串口中接收的数据
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI: // 通讯中断
            case SerialPortEvent.OE: // 溢位错误
            case SerialPortEvent.FE: // 帧错误
            case SerialPortEvent.PE: // 奇偶校验错误
            case SerialPortEvent.CD: // 载波检测
            case SerialPortEvent.CTS: // 清除发送
            case SerialPortEvent.DSR: // 数据设备准备好
            case SerialPortEvent.RI: // 响铃侦测
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 输出缓冲区已清空
                break;
            case SerialPortEvent.DATA_AVAILABLE: // 有数据到达
                // 调用读取数据的方法
                readComm();
                break;
            default:
                break;
        }
    }

    /**
     * 读取串口返回信息
     * @return: void
     */
    public void readComm() {
        try {
            inputStream = serialPort.getInputStream();
            // 通过输入流对象的available方法获取数组字节长度
            byte[] readBuffer = new byte[inputStream.available()];
            // 从线路上读取数据流
            int len = 0;
            while ((len = inputStream.read(readBuffer)) != -1) {
                // 直接获取到的数据
                dataHex = bytesToHexString(readBuffer);
                if(dataHex.startsWith("3052302C")){
                    System.out.println("接收微气象设备的数据： " + data);
                    saveInfo(data);
                    Constant.weatherInfo = data;
                    data = "";
                }
                data = data+ new String(readBuffer, 0, len).trim();
                // 转为十六进制数据

                //System.out.println("data:" + data);
                //System.out.println("dataHex:" + dataHex);// 读取后置空流对象
                inputStream.close();
                inputStream = null;
            }

        } catch (IOException e) {
            logger.info("读取串口数据时发生IO异常");
        }
    }
    public String getUrl(String url, String json) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        String result = "";
        try {
            URI uri = new URIBuilder(url).setParameter("map", json).build();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Content-type", "application/json;charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {e.getMessage();}
        return result;
    }

    private Map<String,Object> saveInfo(String data){
        if("".equals(data)){
            return null;
        }
        DecimalFormat df = new DecimalFormat("#0.0");
        String[] str = data.split(",");
        Map<String,Object> info = new HashMap<String, Object>();
        String windDirection = str[3];//风向 Dx=000D
        windDirection =windDirection.replaceAll("Dx=","").replaceAll("D","");
        info.put("windDirection",windDirection);

        String windSpeed = str[5];//风速 Sm=000.0M
        windSpeed =windSpeed.replaceAll("Sm=","").replaceAll("M","");
        Double temp = Double.valueOf(windSpeed);
        info.put("windSpeed",df.format(temp).toString());
        info.put("windSpeedUnit","m/s");

        String temperature = str[7].replaceAll("Ta=","").replaceAll("C","");//大气温度 Ta=021.2C
        temp = Double.valueOf(temperature);
        info.put("temperature",df.format(temp).toString());
        info.put("temperatureUnit","℃");

        String humidity = str[8].replaceAll("Ua=","").replaceAll("P","");//湿度 Ua=015.7P
        temp = Double.valueOf(humidity);
        info.put("humidity",df.format(temp).toString());
        info.put("humidityUnit","%RH");

        String airPressure = str[9].replaceAll("Pa=","").replaceAll("H","");//气压 Pa=001022.4H;
        temp = Double.valueOf(airPressure)/1000;
        info.put("airPressure",df.format(temp).toString());
        info.put("airPressureUnit","kPa");

        String precipitation = str[10].replaceAll("Rc=","").replaceAll("M","");//降雨量 Rc=0000.0M
        temp = Double.valueOf(precipitation);
        info.put("precipitation",df.format(temp).toString());
        info.put("precipitationUnit","mm");
        //Constant.weatherInfo = info;
        logger.info("发送的信息： "+info);

        JSONObject jsonObj=new JSONObject(info);
        try {
            if("".equals(Constant.path)){
                Constant.path = "192.168.9.40";
            }
            logger.info("发送给： "+Constant.path);
            getUrl("http://"+Constant.path+":18711/homePage/v1/getWeatherInfoForService",jsonObj.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info;


    }

    /**
     * 发送信息到串口
     * @author
     * @date
     * @param: data
     * @return: void
     * @throws
     */
    public void sendComm(String data) {
        byte[] writerBuffer = null;
        try {
            //writerBuffer = hexToByteArray(data);
            //writerBuffer = data.getBytes();
            Integer a = 0x30;
            Integer b = 0x52;
            Integer c = 0x0D;
            Integer d = 0x0A;

            writerBuffer = new byte[]{a.byteValue(), b.byteValue(),c.byteValue(),c.byteValue(),d.byteValue()};
            //writerBuffer = "0R".getBytes();
        } catch (NumberFormatException e) {
            logger.info("命令格式错误！");
        }
        try {
            outputStream = serialPort.getOutputStream();
            outputStream.write(writerBuffer);
            outputStream.flush();
        } catch (NullPointerException e) {
            logger.info("找不到串口。");
        } catch (IOException e) {
            logger.info("发送信息到串口时发生IO异常");
        }
    }

    /**
     * 关闭串口
     * @Description: 关闭串口
     * @param:
     * @return: void
     * @throws
     */
    public void closeSerialPort() {
        if (serialPort != null) {
            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e) {
                    logger.info("关闭输入流时发生IO异常");
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    logger.info("关闭输出流时发生IO异常");
                }
            }
            serialPort.close();
            serialPort = null;
        }
    }

    /**
     * 十六进制串口返回值获取
     */
    public String getDataHex() {
        String result = dataHex;
        // 置空执行结果
        dataHex = null;
        // 返回执行结果
        return result;
    }

    /**
     * 串口返回值获取
     */
    public String getData() {
        String result = data;
        // 置空执行结果
        data = null;
        // 返回执行结果
        return result;
    }

    /**
     * Hex字符串转byte
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte
     */
    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }

    /**
     * hex字符串转byte数组
     * @param inHex 待转换的Hex字符串
     * @return 转换后的byte数组结果
     */
    public static byte[] hexToByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            // 奇数
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            // 偶数
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    /**
     * 数组转换成十六进制字符串
     * @param
     * @return HexString
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}
