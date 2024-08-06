package com.yjh.platform.module.feign;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/17
 * @since [产品/模块版本] （可选）
 */
@FeignClient(name = "iot-center-accessudp")
public interface UdpProxy {
    @ApiOperation(value = "数据召唤")
    @RequestMapping(value = "/sendFile/v1/dataCall", method = RequestMethod.POST)
    void dataCall(@RequestBody Map<String,Object> param);

}
