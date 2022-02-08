package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class MicroWeatherHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到微气象数据了+++++++++++++++++");
        // Deal with robot micro climate data
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
            List<Map<String, String>> weatherList = new ArrayList<>();
            Map<String, String> info = new HashMap<>(5);
            DecimalFormat decimalFormat = new DecimalFormat("#0.0");
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> weatherMap = new HashMap<>(16);
                weatherMap.put("robotName", res.get("robot_name").toString());
                weatherMap.put("robotCode", robotCode);
                weatherMap.put("time", res.get("time").toString());
                weatherMap.put("type", res.get("type").toString());
                weatherMap.put("value", res.get("value").toString());
                weatherMap.put("valueUnit", res.get("value_unit").toString());
                weatherMap.put("unit", res.get("unit").toString());
                weatherList.add(weatherMap);
                // 1=温度 2=湿度 3=风速 4=大气压  5=降雨量 6=风向
                String weatherType = weatherMap.get("type");
                if ("1".equals(weatherType)) {
                    info.put("temperature", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
                    info.put("temperatureUnit", "℃");
                }
                if ("2".equals(weatherType)) {
                    info.put("humidity", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
                    info.put("humidityUnit", "%");
                }
                if ("3".equals(weatherType)) {
                    info.put("windSpeed", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
                    info.put("windSpeedUnit", "m/s");
                }
                if ("4".equals(weatherType)) {
                    info.put("airPressure", decimalFormat.format(Double.valueOf(weatherMap.get("value")) / 10));
                    info.put("airPressureUnit", "kPa");
                }
                if ("5".equals(weatherType)) {
                    info.put("precipitation", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
                    info.put("precipitationUnit", "mm");
                }
                if ("6".equals(weatherType)) {
                    if ("".equals(weatherMap.get("value")) || null == weatherMap.get("value")) {
                        info.put("windDirection", "--");
                    } else {
                        info.put("windDirection", weatherMap.get("value"));
                    }

                    // info.put("precipitationUnit","mm");
                }
            });

            for (int i = 0; i < weatherList.size(); i++) {
                redisTemplate.opsForHash().putAll("RobotWeather:" + robotCode + ":" + weatherList.get(i).get("type"), weatherList.get(i));
            }

            Constant.weatherServer(info, Constant.WEATHER_URL);
            String weatherXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] weatherProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, weatherXmlString);
            RobotServerHandler.send(weatherProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了", robotCode);
        }
        // 国网要求
        // robotService.upToCruise(xmlBaseModel);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MICRO_WEATHER_DATA.getCode(), this);
    }
}
