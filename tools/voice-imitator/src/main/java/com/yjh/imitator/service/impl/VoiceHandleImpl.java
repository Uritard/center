/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.yjh.imitator.common.HttpUtil;
import com.yjh.imitator.common.JsonUtil;
import com.yjh.imitator.config.VoiceProperties;
import com.yjh.imitator.constant.Constant;
import com.yjh.imitator.pojo.*;
import com.yjh.imitator.service.IVoiceHandle;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceHandleImpl implements IVoiceHandle {
    private final VoiceProperties voiceProperties;

    private static final AtomicInteger COLLECT_COUNT = new AtomicInteger(0);
    private static final AtomicInteger ANALYSE_COUNT = new AtomicInteger(0);

    private static final AtomicInteger RESPONSE_COUNT = new AtomicInteger(0);

    private volatile static boolean running = true;

    @Override
    public boolean addCollect(VoiceRequest<Collect> body) {
        int count = COLLECT_COUNT.incrementAndGet();

        if (voiceProperties.isRetError() && count % 3 == 0) {
            log.info("声音数据采集失败, count: {}", count);
            return false;
        }
        RespSource respSource = body.getRespSource();

        List<Collect> collectList = body.getObjectList();
        for (Collect c : collectList) {
            VOICE_COLLECT.add(new DelayTask<>(respSource, c, Duration.ofSeconds(c.getDuration())));
        }

        return true;
    }

    @Override
    public boolean addAnalyse(VoiceRequest<Analyse> body) {
        int count = ANALYSE_COUNT.incrementAndGet();

        if (voiceProperties.isRetError() && count % 3 == 0) {
            log.info("声音数据分析失败, count: {}", count);
            return false;
        }
        RespSource respSource = body.getRespSource();

        List<Analyse> collectList = body.getObjectList();
        for (Analyse c : collectList) {
            int rand = RandomUtil.randomInt(600, 1600);
            VOICE_ANALYSE.add(new DelayTask<>(respSource, c, Duration.ofMillis(rand)));
        }

        return true;
    }

    public List<File> files() {
        try {
            String dir = voiceProperties.getVoicePath();
            File root = ResourceUtils.getFile(ResourceUtils.FILE_URL_PREFIX + dir);
            if (!root.exists()) {
                root = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + dir);
            }
            if (root.exists()) {
                File[] files = root.listFiles((dir1, name) -> StringUtils.endsWithIgnoreCase(name, "wav"));
                if (files != null && files.length > 0) {
                    return Arrays.asList(files);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public void responseCollect(List<Pair<RespSource, Collect>> takeList) {
        List<CollectNotify> notifyList = getCollectNotifyList();

        Map<RespSource, List<Collect>> maps =
            takeList.stream().collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));
        maps.forEach((respSource, collectList) -> {
            VoiceResponse<CollectNotify> response = new VoiceResponse<>();
            response.setRequestId(respSource.getRequestId());

            List<ResponseItem<CollectNotify>> resultList = new ArrayList<>();
            collectList.forEach(c -> {
                ResponseItem<CollectNotify> responseItem = new ResponseItem<>();
                responseItem.setObjectId(c.getObjectId());
                int code = voiceProperties.isRetError() && RandomUtil.randomInt(10) < 3 ? 2004 : 2000;

                if (notifyList.isEmpty()) {
                    code = 2003;
                } else if (code == 2000) {
                    List<CollectNotify> results = new ArrayList<>();
                    int count = RESPONSE_COUNT.getAndIncrement();
                    int size = notifyList.size();
                    int i = count % size;

                    // if (voiceProperties.isRetError() && size - i == 1) {
                    //     results.addAll(notifyList);
                    // } else {
                    results.add(notifyList.get(i));
                    // }

                    responseItem.setResults(results);
                }
                responseItem.setCode(code);

                resultList.add(responseItem);
            });
            response.setResultList(resultList);

            String notifyUrl =
                StrUtil.format(voiceProperties.getCollectNotify(), respSource.getRequestHostIp(), respSource.getRequestHostPort());

            String ret = HttpUtil.postUrl(notifyUrl, JsonUtil.toJson(response));
            log.info("数据采集通知返回结果, requestId: {} -- {}", respSource.getRequestId(), ret);
        });

    }

    private List<CollectNotify> getCollectNotifyList() {
        List<CollectNotify> results = new ArrayList<>();
        List<File> files = files();
        String prefix = StringUtils.removeEnd(voiceProperties.getFileUrlPath(), "/");

        for (File f : files) {
            String name = f.getName();
            String fileUrl = prefix + "/" + name;
            CollectNotify notify = new CollectNotify().setFilename(name).setFileUrl(fileUrl);
            results.add(notify);
        }
        return results;
    }

    public void responseAnalyse(List<Pair<RespSource, Analyse>> takeList) {

        Map<RespSource, List<Analyse>> maps =
            takeList.stream().collect(Collectors.groupingBy(Pair::getKey, Collectors.mapping(Pair::getValue, Collectors.toList())));
        maps.forEach((respSource, collectList) -> {
            VoiceResponse<AnalyseNotify> response = new VoiceResponse<>();
            response.setRequestId(respSource.getRequestId());

            List<ResponseItem<AnalyseNotify>> resultList = new ArrayList<>();
            collectList.forEach(c -> {
                ResponseItem<AnalyseNotify> responseItem = new ResponseItem<>();
                responseItem.setObjectId(c.getObjectId());
                int code = voiceProperties.isRetError() && RandomUtil.randomInt(10) < 3 ? 2002 : 2000;

                int count = RESPONSE_COUNT.getAndIncrement();
                if (voiceProperties.isRetError() && code == 2000 && count % 3 == 1) {
                    code = 2001;
                } else if (code == 2000) {
                    List<AnalyseNotify> results = new ArrayList<>();
                    if (count % 3 == 0) {
                        AnalyseNotify notify = new AnalyseNotify();
                        notify.setConf(0.0000F).setStartTime(0.0F).setEndTime(0.0F).setType("").setDesc("未检测出缺陷").setValue("0");
                        results.add(notify);
                    } else {
                        String[] types = c.getTypeList();
                        int defectCount = RandomUtil.randomInt(1, 4);
                        for (int i = 0; i < defectCount; i++) {
                            try {
                                int j = RandomUtil.randomInt(types.length);
                                String type = types[j];
                                AnalyseNotify notify = new AnalyseNotify();
                                notify.setType(type).setDesc(EnumUtils.getEnum(Constant.VoiceDefect.class, StringUtils.upperCase(type)).getDesc()).setValue("1");
                                float startTime = NumberUtil.round(RandomUtil.randomDouble(0.1D, 20.0D), 1).floatValue();
                                float endTime = NumberUtil.round(RandomUtil.randomDouble(30.0D, 50.0D), 1).floatValue();
                                float conf = NumberUtil.round(RandomUtil.randomDouble(0.7D, 1.0D), 4).floatValue();
                                notify.setStartTime(startTime).setEndTime(endTime).setConf(conf);
                                results.add(notify);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                    responseItem.setResults(results);
                }
                responseItem.setCode(code);

                resultList.add(responseItem);
            });
            response.setResultList(resultList);

            String notifyUrl =
                StrUtil.format(voiceProperties.getAnalyseNotify(), respSource.getRequestHostIp(), respSource.getRequestHostPort());

            String ret = HttpUtil.postUrl(notifyUrl, JsonUtil.toJson(response));
            log.info("数据分析通知返回结果, requestId: {} -- {}", respSource.getRequestId(), ret);
        });
    }

    @PostConstruct
    public void threadStart() {
        ThreadUtil.execAsync(new VoiceCollectThread(this));
        ThreadUtil.execAsync(new VoiceAnalyseThread(this));
    }

    public <T> List<Pair<RespSource, T>> takes(DelayQueue<DelayTask<T>> queue) {
        List<Pair<RespSource, T>> takeList = new ArrayList<>();
        try {
            DelayTask<T> task = queue.take();
            takeList.add(Pair.of(task.getRespSource(), task.getContent()));
            if (voiceProperties.isBatchResponse()) {
                TimeUnit.MILLISECONDS.sleep(500);
                List<DelayTask<T>> tasks = new ArrayList<>(30);
                int ret = queue.drainTo(tasks, 29);
                if (ret > 0) {
                    takeList.addAll(tasks.stream().map(t -> Pair.of(t.getRespSource(), t.getContent())).toList());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
        return takeList;
    }

    static class VoiceCollectThread implements Runnable {
        final VoiceHandleImpl voiceHandle;

        VoiceCollectThread(VoiceHandleImpl voiceHandle) {
            this.voiceHandle = voiceHandle;
        }

        @Override
        public void run() {
            log.info("VoiceCollectThread started, running: {}", running);
            while (running) {
                try {
                    DelayTask<Collect> task = VOICE_COLLECT.take();
                    // List<Pair<RespSource, Collect>> takeList = voiceHandle.takes(VOICE_COLLECT);
                    voiceHandle.responseCollect(Collections.singletonList(Pair.of(task.getRespSource(), task.getContent())));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            log.info("VoiceCollectThread stoped, running: {}", running);
        }
    }

    static class VoiceAnalyseThread implements Runnable {
        final VoiceHandleImpl voiceHandle;

        VoiceAnalyseThread(VoiceHandleImpl voiceHandle) {
            this.voiceHandle = voiceHandle;
        }

        @Override
        public void run() {
            log.info("VoiceAnalyseThread started, running: {}", running);
            while (running) {
                try {
                    List<Pair<RespSource, Analyse>> takeList = voiceHandle.takes(VOICE_ANALYSE);
                    voiceHandle.responseAnalyse(takeList);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            log.info("VoiceAnalyseThread stoped, running: {}", running);
        }
    }
}
