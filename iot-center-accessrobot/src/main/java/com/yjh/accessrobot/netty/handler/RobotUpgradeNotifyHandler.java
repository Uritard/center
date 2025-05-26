package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.TRobotInfo;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.entiy.UpgradeResult;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.yjh.accessrobot.module.command.service.RobotService.SYNC_MODE_CACHE;

/**
 * RobotUpgradeNotifyHandler: 处理版本更新通知
 *
 * @author shaobinfen
 * @date 2025/05/23
 */
@Service
@Slf4j
public class RobotUpgradeNotifyHandler implements MessageHandlerStrategy, InitializingBean {

    @Resource
    private TRobotInfoDao tRobotInfoDao;

    //更新状态: Sucess-成功 fail-失败
    private final static String UPGRADE_STATE = "program_update_state";

    //版本号
    private final static String PROGRAM_VERSION = "Program_version";

    private final static String SUCCESS = "Sucess";

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        String robotCode = xmlBaseModel.getSendCode();
        log.info("巡视主机收到机器人: {} 的版本更新通知", robotCode);
        try {
            if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
                Optional<UpgradeResult> upgradeResult = checkAndGetUpgradeResult(xmlBaseModel.getItems());
                upgradeResult.ifPresent(result -> {
                    log.info(result.toString());
                    TRobotInfo robotInfo = tRobotInfoDao.selectRobotInfoByCode(robotCode);
                    if (StringUtils.equals(SUCCESS, result.getUpgradeState())) {
                        robotInfo.setSystemVersion(result.getProgramVersion());
                        //todo 暂时先在t_robot_info表中添加system_version字段, 后面可以考虑新建一个表单独维护机器人系统版本信息
                        tRobotInfoDao.update(robotInfo);
                        Constant.sendProcess(robotCode, robotInfo.getRobotName() + "远程升级", 1, "远程升级成功");
                    } else {
                        Constant.sendProcess(robotCode, robotInfo.getRobotName() + "远程升级", 0, "远程升级失败");
                    }
                });
            } else {
                log.error("机器人: {}未注册!", robotCode);
            }
        } catch (Exception e) {
            log.error("处理版本更新通知消息异常", e);
        } finally {
            SYNC_MODE_CACHE.remove(robotCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.UPGRADE_NOTIFICATION.getCode(), this);
    }

    public Optional<UpgradeResult> checkAndGetUpgradeResult(List<Map<String, Object>> items) {
        return Optional.ofNullable(items)
                .filter(CollectionUtils::isNotEmpty)
                .map(list -> list.get(0))
                .flatMap(item -> {
                    Object upgradeState = item.get(UPGRADE_STATE);
                    Object programVersion = item.get(PROGRAM_VERSION);
                    if (Objects.nonNull(upgradeState) && Objects.nonNull(programVersion)) {
                        return Optional.of(new UpgradeResult(upgradeState.toString(), programVersion.toString()));
                    }
                    return Optional.empty();
                });
    }
}