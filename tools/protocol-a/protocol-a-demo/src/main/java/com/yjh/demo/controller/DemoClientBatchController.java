package com.yjh.demo.controller;

import com.alibaba.fastjson.JSONObject;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.entity.BatchMessageParam;
import com.yjh.demo.task.SendTask;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.protocol_a.MessageIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: DemoClientBatchController
 * @Description:
 * @author: yanhao
 * @date: 2022/8/27
 */
@RestController
@RequestMapping("/demo-client-batch")
@Slf4j
public class DemoClientBatchController {
    @Autowired
    private RxBus clientBatchRxBus;

    @Autowired
    private MsgChannel clientBatchMsgChannel;

    @Autowired
    private ExecutorService clientBatchOutboundExecutor;

    public static Map<String, String[]> deviceArrayMap = new ConcurrentHashMap<>();
    public static Map<String, String> taskCodeMap = new ConcurrentHashMap<>();
    public static Map<String, String> taskNameMap = new ConcurrentHashMap<>();

    public static AtomicInteger errorCount;


    public static AtomicInteger finishCount;

    public static int totalCount;


    @PostMapping(value = "/batchsendtest")
    public String batchSendTest(@RequestBody BatchMessageParam batchMessageParam) {
        errorCount = new AtomicInteger(0);
        finishCount = new AtomicInteger(0);
        totalCount = batchMessageParam.getTaskCount() * batchMessageParam.getThreadCount();

        ExecutorService executorService = Executors.newFixedThreadPool(batchMessageParam.getThreadCount());
        MessageIdGenerator messageIdGenerator = new MessageIdGenerator();
        long startTime = System.currentTimeMillis();
        //获取文件byte[]
        File file = new File(batchMessageParam.getFilePath());
        byte[] data;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            data = bos.toByteArray();
            bos.close();
        } catch (Exception e) {
            log.error("读取文件失败", e);
            return "读取文件失败";
        }

        List<String> sendCodeList = DemoClientTaskContoller.getSendCode();
        for (int i = 0; i < batchMessageParam.getThreadCount(); i++) {
            int idx = i % sendCodeList.size();
            String sendCode = sendCodeList.get(idx);
            SendTask sendTask = new SendTask(i, sendCode, batchMessageParam, clientBatchOutboundExecutor, messageIdGenerator, data, clientBatchRxBus, clientBatchMsgChannel);
            executorService.execute(sendTask);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.error("等待处理完成系统异常", e);
        }
        log.info("并发线程数:{} ,总任务数:{}  耗时 {}ms", batchMessageParam.getThreadCount(), batchMessageParam.getTaskCount(), System.currentTimeMillis() - startTime);
        return "ok";
    }

    @GetMapping("/queryResult")
    public String queryResult() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "200");
        jsonObject.put("errorCount", errorCount.intValue());
        jsonObject.put("finishCount", finishCount.intValue());
        jsonObject.put("totalCount", totalCount);
        return jsonObject.toJSONString();
    }


}
