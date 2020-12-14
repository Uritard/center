package com.yjh.platform.module.task.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.task.service.HomePageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author lqh
 * @since 2020/12/14
 */
@RestController
@RequestMapping("/homePage/v1")
@Api(value = "/homePage", description = "首页相关接口")
public class HomePageController {

    private Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Autowired
    private HomePageService homePageService;

    @ApiOperation(value = "巡视任务数据概览")
    @RequestMapping(value = "/taskInfo", method = RequestMethod.GET)
    public Result taskInfo(@RequestParam(value = "date", required = false) Integer date) {
        Result result = new Result();
        try {
            result.setData(homePageService.taskInfo(date));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取巡视任务数据概览错误:", e);
        }
        return result;
    }
}
