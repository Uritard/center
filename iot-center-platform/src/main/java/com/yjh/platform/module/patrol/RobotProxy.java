package com.yjh.platform.module.patrol;

import com.yjh.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/16
 * @since [产品/模块版本] （可选）
 */
@FeignClient(name = "iot-center-accessrobot")
public interface RobotProxy {
    @PostMapping(value = "robot/v1/deleteTransfer")
    public Result deleteTransfer(@RequestParam(value = "edgeCode") List<String> edgeCode,
        @RequestParam(value = "taskId") String taskId,
        @RequestParam(value = "startTime") String startTime,
        @RequestParam(value = "source") String source);
}
