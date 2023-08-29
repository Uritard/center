package com.yjh.platform.module.file;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2022/08/30
 */
@RestController
@RequestMapping("/file/v1")
@Slf4j
@Api(value = "/file", tags = "文件拷贝")
public class FileController {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 算法主机-缺陷告警
     *
     * @return ResponseEntity
     */
    @PostMapping(value = "/copy-ftps")
    public void copyFtpsToLocal(@RequestBody Map<String, String> map) {
        String ftpsPath = map.get("ftpsPath");
        String localPath = map.get("localPath");
        log.info("ftpsPath:{} localPath: {}", ftpsPath, localPath);
        Map<String, String> filePathMap = redisTemplate.opsForHash().entries("t_sys_param:ftpsFilePath");
        ftpsPath = filePathMap.get("content") + "/" + ftpsPath;
        // File ff = new File(localPath);
        // if (!ff.getParentFile().exists()) {
        //     ff.getParentFile().setWritable(true, false);
        //     ff.getParentFile().mkdirs();
        // }
        try {
            log.info("要拷贝的路径：{}", ftpsPath);
            FileUtil.copyFileUsingIOUtils(ftpsPath, localPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
