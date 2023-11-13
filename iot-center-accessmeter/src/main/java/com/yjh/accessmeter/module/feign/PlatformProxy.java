package com.yjh.accessmeter.module.feign;

import com.yjh.accessmeter.module.device.entity.TMeter;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/17
 * @since [产品/模块版本] （可选）
 */
@FeignClient(name = "iot-center-platform")
public interface PlatformProxy {
    @ApiOperation(value = "电表信息上报")
    @RequestMapping(value = "/tMeter/v1/uploadMeterInfo", method = RequestMethod.POST)
    void uploadMeterInfo(@RequestBody TMeter tMeter);
}
