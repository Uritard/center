package com.yjh.accessrobot.module.command.proxy;

import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.EnvDeviceStatus;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/17
 * @since [产品/模块版本] （可选）
 */
@FeignClient(name = "iot-center-platform")
public interface PlatformProxy {
    @ApiOperation(value = "删除")
    @RequestMapping(value = "/uPatrolTask/v1/delete", method = RequestMethod.POST)
    Result delete(@RequestParam(value = "taskId") String taskId,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "source", required = false) String source
    );

    @ApiOperation(value = "下级上报环控设备信息入库")
    @RequestMapping(value = "/tIotDeviceData/v1/envData", method = RequestMethod.POST)
    Result insertEnvData(List<EnvDeviceStatus> envDeviceStatusList);
}
