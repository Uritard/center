package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSONObject;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.EnvDeviceStatus;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Slf4j
@Service
public class MicroWeatherHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RobotService robotService;


    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的微气象数据了+++++++++++++++++");
        // Deal with robot micro climate data
        String robotCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String weatherXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] weatherProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, weatherXmlString);
        RobotServerHandler.send(weatherProtocol, robotCode);
        log.info("本级系统给下级{}响应了", robotCode);

        List<Map<String, String>> weatherList = new ArrayList<>();
        Map<String, String> info = new HashMap<>();
        info.put("robotCode", robotCode);
        DecimalFormat decimalFormat = new DecimalFormat("#0.0");
        List<EnvDeviceStatus> envDeviceStatusList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> weatherMap = new HashMap<>(16);
            // 2022过检 robot_name -> patroldevice_name
            weatherMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            weatherMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            weatherMap.put("robotCode", robotCode);
            weatherMap.put("time", res.get("time").toString());
            weatherMap.put("type", res.get("type").toString());
            weatherMap.put("unit", res.get("unit").toString());
            String valueTemp = new DecimalFormat("#0.00").format(NumberUtils.toDouble(res.get("value").toString()));
            weatherMap.put("value", valueTemp);
            weatherMap.put("valueUnit", valueTemp + weatherMap.get("unit"));

            weatherList.add(weatherMap);
            // 2022过检 环境类型修改
            // 1:环境温度 2:环境湿度 3:=风速 4:=雨量 5:=风向 6:=气压 7:=氧气 8:=SF6
            String weatherType = weatherMap.get("type");
            if ("1".equals(weatherType)) {
                info.put("temperature", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
//                    info.put("temperatureUnit", "℃");
                info.put("temperatureUnit",weatherMap.get("unit"));
            }
            if ("2".equals(weatherType)) {
                info.put("humidity", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
//                    info.put("humidityUnit", "%");
                info.put("humidityUnit", weatherMap.get("unit"));
            }
            if ("3".equals(weatherType)) {
                info.put("windSpeed", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
//                    info.put("windSpeedUnit", "m/s");
                info.put("windSpeedUnit", weatherMap.get("unit"));
            }
            if ("4".equals(weatherType)) {
                info.put("precipitation", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
//                    info.put("precipitationUnit", "mm");
                info.put("precipitationUnit", weatherMap.get("unit"));
            }
            if ("5".equals(weatherType)) {
                if ("".equals(weatherMap.get("value")) || null == weatherMap.get("value")) {
                    info.put("windDirection", "--");
                } else {
                    info.put("windDirection", weatherMap.get("value"));
                }
                // info.put("precipitationUnit","mm");
            }
            if ("6".equals(weatherType)) {
                info.put("airPressure", decimalFormat.format(Double.valueOf(weatherMap.get("value"))));
//                    info.put("airPressureUnit", "kPa");
                info.put("airPressureUnit", weatherMap.get("unit"));
            }
            if ("7".equals(weatherType)) {
                info.put("oxygen", weatherMap.get("value"));
//                    info.put("oxygenUnit", "ppm");
                info.put("oxygenUnit", weatherMap.get("unit"));
            }
            if ("8".equals(weatherType)) {
                info.put("sf6", weatherMap.get("value"));
                info.put("sf6Unit", weatherMap.get("unit"));
            }

            //环控数据组装
            String value = String.valueOf(res.get("value"));
            if (res.containsKey("value_type")) {
                EnvDeviceStatus env = new EnvDeviceStatus();
                env.setRobotCode(robotCode);
                env.setUnit(res.get("unit").toString());
                env.setDeviceName(res.get("device_name").toString());
                env.setDeviceId(res.get("type_device_num").toString());
                env.setShowType(res.get("value_type").toString());
                env.setType(res.get("type").toString());
                //1.状态型 2.数值型 3.控制型 (0开 1关)
                if ("1".equals(res.get("value_type").toString())) {
                    env.setStatus(Integer.parseInt(value));
                    if ("0".equals(value)) {
                        env.setDeviceValue("正常");
                    } else {
                        env.setDeviceValue("异常");
                    }
                } else if ("2".equals(res.get("value_type").toString())) {
                    env.setStatus(0);
                    env.setDeviceValue(value);
                } else {
                    //空调  value="1,2"  开关，制冷制热
                    if ("7".equals(res.get("type").toString())) {
                        String[] strings = value.split(",");
                        if ("0".equals(strings[0])) {  //空调开启
                            if (strings.length > 1 && "1".equals(strings[1])) { //制冷制热
                                env.setDeviceValue("制热");
                            } else {
                                env.setDeviceValue("制冷");
                            }
                        } else {
                            env.setDeviceValue("关闭");
                        }
                        env.setStatus(Integer.parseInt(strings[0]));//空调的开关
                    } else {
                        if ("0".equals(value)) {
                            env.setDeviceValue("开启");
                        } else {
                            env.setDeviceValue("关闭");
                        }
                        env.setStatus(Integer.parseInt(value));
                    }
                }
                envDeviceStatusList.add(env);
            }
        });

        if (envDeviceStatusList.size() > 0) {
            JSONObject json = new JSONObject();
            json.put("envDeviceStatusList", envDeviceStatusList);
            json.put("robotCode", xmlBaseModel.getSendCode());
            log.info("环境数据上报集控" + json);
            robotService.addWeatherInfo(json);
        }


        for (int i = 0; i < weatherList.size(); i++) {
            String robotWeather = "RobotWeather:" + robotCode + ":" + weatherList.get(i).get("type");
            String stationWeather = "stationWeather:" + weatherList.get(i).get("type");
            redisTemplate.opsForHash().putAll(robotWeather, weatherList.get(i));
            redisTemplate.expire(robotWeather, 7, TimeUnit.DAYS);
            redisTemplate.opsForHash().putAll(stationWeather, weatherList.get(i));
            redisTemplate.expire(stationWeather, 7, TimeUnit.DAYS);
        }
        System.out.println("微气象数据测试一波++++++++" + info);
        Constant.mapToOtherServer(info, Constant.WEATHER_URL);

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.MICRO_WEATHER_DATA.getCode(), this);
    }
}
