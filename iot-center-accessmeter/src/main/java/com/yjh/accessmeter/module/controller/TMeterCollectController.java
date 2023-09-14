package com.yjh.accessmeter.module.controller;

import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.logs.Logs;
import com.yjh.accessmeter.module.service.TMeterCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/tMeterCollect/v1")
@Api(value = "/tMeterCollect", description = "电表操作接口")
@Slf4j
public class TMeterCollectController {
    @Autowired
    private TMeterCollectService tMeterCollectService;

    @ApiOperation(value = "新增电表配置")
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @Logs(title = "新增电表配置",content = "新增电表配置",logType = 2,authority = "1234")
    public Result add(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            tMeterCollectService.add(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加电表失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "删除电表")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @Logs(title = "删除电表",content = "新增电表配置",logType = 2,authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            tMeterCollectService.delete(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加电表失败：", e);
        }
        return result;
    }


    @ApiOperation(value = "采集电量")
    @RequestMapping(value = "/collect", method = RequestMethod.GET)
    @Logs(title = "采集电量",content = "采集电量",logType = 2,authority = "1234")
    public  Result collectData(@RequestParam("id")Long id){
        Result result = new Result();
        try {
            tMeterCollectService.collect(id);;
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("采集电量失败", e);
        }
        return result;
    }
}
