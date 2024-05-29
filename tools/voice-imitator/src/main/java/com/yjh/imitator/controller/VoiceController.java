package com.yjh.imitator.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.yjh.imitator.common.JsonUtil;
import com.yjh.imitator.common.Result;
import com.yjh.imitator.config.VoiceProperties;
import com.yjh.imitator.pojo.Analyse;
import com.yjh.imitator.pojo.Collect;
import com.yjh.imitator.pojo.VoiceProp;
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
        Result result;
        log.info("========= voiceprintDataCollect params: {}", JsonUtil.toJson(body));
        boolean ret = voiceHandle.addCollect(body);
        if (ret) {
            result = Result.ofSuccess();
        } else {
            result = Result.ofError(500);
        }

        log.info("========= voiceprintDataCollect result: {}", JsonUtil.toJson(result));
        return result;
    }

    @Operation(summary = "声纹数据分析")
    @PostMapping(value = "/voiceprintAnalyse")
    public Result voiceprintAnalyse(@RequestBody VoiceRequest<Analyse> body) {
        Result result;
        log.info("========= voiceprintAnalyse params: {}", JsonUtil.toJson(body));
        boolean ret = voiceHandle.addAnalyse(body);
        if (ret) {
            result = Result.ofSuccess();
        } else {
            result = Result.ofError(500);
        }
        log.info("========= voiceprintDataCollect result: {}", JsonUtil.toJson(result));
        return result;
    }

    @Operation(summary = "声纹配置")
    @PostMapping(value = "/voiceProperties")
    public VoiceProp voiceProperties(@RequestBody VoiceProp body) {

        log.info("========= voiceProperties params: {}", JsonUtil.toJson(body));
        try {
            BeanUtil.copyProperties(body, voiceProperties, CopyOptions.create(VoiceProperties.class, true));

            BeanUtil.copyProperties(voiceProperties, body);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return body;
    }
}
