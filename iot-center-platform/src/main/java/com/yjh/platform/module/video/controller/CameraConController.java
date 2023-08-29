package com.yjh.platform.module.video.controller;

import com.yjh.platform.module.video.service.CameraConService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/8/28
 * @since [产品/模块版本] （可选）
 */

@RestController
@RequestMapping("/camera/v1")
@Api(value = "/cameraControl", tags = "相机接口")
public class CameraConController {

    private final Logger LOGGER = LoggerFactory.getLogger(CameraConController.class);

    @Resource
    private CameraConService cameraConService;

}
