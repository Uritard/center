package com.yjh.imitator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.yjh.imitator.common.JsonUtil;
import com.yjh.imitator.common.Result;
import com.yjh.imitator.config.VoiceProperties;
import com.yjh.imitator.pojo.Analyse;
import com.yjh.imitator.pojo.Collect;
import com.yjh.imitator.pojo.VoiceRequest;
import com.yjh.imitator.service.impl.VoiceHandleImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/1
 * @since [产品/模块版本] （可选）
 */
@Tag(name = "声纹服务")
@RestController
@RequestMapping("/")
@Slf4j
@RequiredArgsConstructor
public class VoiceController {
    private final VoiceHandleImpl voiceHandle;
    private final VoiceProperties voiceProperties;

    @Operation(summary = "声纹数据采集")
    @PostMapping(value = "/voiceprintDataCollect")
    public Result voiceprintDataCollect(@RequestBody VoiceRequest<Collect> body) {

        log.info("========= voiceprintDataCollect params: {}", body);
        boolean ret = voiceHandle.addCollect(body);
        if (ret) {
            return Result.ofSuccess();
        } else {
            return Result.ofError(500);
        }

    }

    @Operation(summary = "声纹数据分析")
    @PostMapping(value = "/voiceprintAnalyse")
    public Result voiceprintAnalyse(@RequestBody VoiceRequest<Analyse> body) {

        log.info("========= voiceprintAnalyse params: {}", body);
        boolean ret = voiceHandle.addAnalyse(body);
        if (ret) {
            return Result.ofSuccess();
        } else {
            return Result.ofError(500);
        }
    }

    @Operation(summary = "声纹配置")
    @PostMapping(value = "/voiceProperties")
    public String voiceProperties(@RequestBody Map<String, Object> body) {

        log.info("========= voiceProperties params: {}", body);
        try {
            BeanUtil.copyProperties(body, voiceProperties, CopyOptions.create(VoiceProperties.class, true));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return voiceProperties.toString();
    }
}
