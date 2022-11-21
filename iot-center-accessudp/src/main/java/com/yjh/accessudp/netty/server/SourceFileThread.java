package com.yjh.accessudp.netty.server;

import com.yjh.accessudp.common.Constant;
import com.yjh.accessudp.commons.utils.file.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @author hyh
 * @since 2022/8/19
 **/
@Slf4j
public class SourceFileThread implements Runnable{

    private final String data;

    private final RedisTemplate redisTemplate;

    public SourceFileThread(String data, RedisTemplate redisTemplate){
        this.data = data;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run() {
        //todo 设备资源信息配置文件
        log.info("上传设备资源信息配置文件到服务器");
        String path = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:sourceFilePath").get("content"));
        FileUtil.createDirectory(path);
        String fileName = "source_file_model.cime";
        File file = new File(path + fileName);
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter out = new BufferedWriter(fileWriter)){
            out.write(data);
            out.flush();
        }catch (Exception e){
            log.error("文件生成失败！", e);
        }
        //有变动 同步模型
        log.info("Synchronize the device resource config file to the patrol host...");
        Constant.modelUpload("10");
    }
}
