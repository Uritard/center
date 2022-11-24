package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.enumeration.AlarmLevelEnum;
import com.yjh.accessrobot.commons.utils.file.FileUtil;
import com.yjh.accessrobot.module.command.dao.TStdDeviceMapper;
import com.yjh.accessrobot.module.command.dao.TWarnInfoMapper;
import com.yjh.accessrobot.module.command.entity.TStdDevice;
import com.yjh.accessrobot.module.command.entity.TWarnInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;

/**
 * <功能描述> 上级系统接收巡视主机消息
 *
 * @author yanhao
 * @date 2022/11/23
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class SilentMonitoringHandlerUpSystem  implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private TStdDeviceMapper tStdDeviceMapper;
    @Autowired
    private TWarnInfoMapper tWarnInfoMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("上级系统接收到静默告警数据 xmlBaseModel:{}", JSON.toJSONString(xmlBaseModel));
        String sendCode = xmlBaseModel.getSendCode();
        if (!Constant.robotRegisterFlag.getOrDefault(sendCode, false)) {
            log.error("robotRegisterFlag not exist,sendCode:{} ",sendCode);
            return;
        }
        String filePath = String.valueOf(xmlBaseModel.getItems().get(0).get("file_path"));
        String deviceId = String.valueOf(xmlBaseModel.getItems().get(0).get("patroldevice_code"));
        String content= String.valueOf(xmlBaseModel.getItems().get(0).get("content"));
        String alaramLevel= String.valueOf(xmlBaseModel.getItems().get(0).get("alarm_level"));

        TStdDevice tStdDevice=tStdDeviceMapper.selectByEdgeCodeAndOriginId(sendCode,deviceId);
        if(tStdDevice==null){
            log.error("tStdDevice is null,edgeCode:{}, deviceId:{} ",sendCode,deviceId);
            return;
        }
        // 图片在ftps上的全路径
        String resultAbsolutePath = redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content") + filePath;
        String targetPath = redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content") + filePath;
        try {
            FileUtil.copyFileUsingStream(resultAbsolutePath, targetPath);
        } catch (IOException e) {
            log.error("复制文件失败，resultAbsolutePath:{}  targetPath:{} ", resultAbsolutePath, targetPath, e);
            return ;
        }
        String defectResultRealImg = targetPath.replaceAll(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultImg", "content")),
                String.valueOf(redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg", "content")));
        TWarnInfo tWarnInfo = new TWarnInfo()
                .setWarnLevel(Integer.valueOf(AlarmLevelEnum.getAlarmLevelByProtocolCode(alaramLevel).getCode()))
                .setWarnTime(new Date())
                .setWarnName("静默监视告警数据")
                .setWarnContent(content)
                .setDeviceId(tStdDevice.getDeviceId())
                .setConfMode(276)
                .setDefectModel(450)
                .setAlarmSource(800)
                .setImagePath(defectResultRealImg);
        tWarnInfoMapper.insert(tWarnInfo);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.SILENT_MONITORING_ALARM.getCode(), this);
    }
}
