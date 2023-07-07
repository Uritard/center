package com.yjh.platform.module.user.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TStdRegion;
import com.yjh.platform.module.user.dao.TWiringDiagramDao;
import com.yjh.platform.module.user.entity.TWiringDiagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * @author 丫C
 * @since 2023-06-19
 */
@Service
public class TWiringDiagramService {

    private final TWiringDiagramDao tWiringDiagramDao;
    private final RedisTemplate<String, ?> redisTemplate;
    private final TStdRegionDao tStdRegionDao;
    private final TWiringConfigService tWiringConfigService;

    public TWiringDiagramService(TWiringDiagramDao tWiringDiagramDao, RedisTemplate<String, ?> redisTemplate, TStdRegionDao tStdRegionDao,
                                 TWiringConfigService tWiringConfigService) {
        this.tWiringDiagramDao = tWiringDiagramDao;
        this.redisTemplate = redisTemplate;
        this.tStdRegionDao = tStdRegionDao;
        this.tWiringConfigService = tWiringConfigService;
    }

    private final Logger log = LoggerFactory.getLogger(TWiringDiagramService.class);

    @Transactional(rollbackFor = Exception.class)
    public Result insert(MultipartFile file, Long regionId, String regionName, Long userId, Result result) {
        if (file == null) {
            result.setMessage(209, "文件错误,请重试");
            return result;
        }
        if (-1 == regionId) {
            TStdRegion tStdRegion = tStdRegionDao.selectRootRegion();
            regionId = tStdRegion.getRegionId();
            regionName = tStdRegion.getRegionName();
        }
        TWiringDiagram tWiringDiagramTemp = selectByCondition(regionId);
        if (Objects.nonNull(tWiringDiagramTemp)) {
            result.setMessage(209, "同一站所/区域只能上传一张图");
            return result;
        }

        TWiringDiagram tWiringDiagram = new TWiringDiagram();
        tWiringDiagram.setRegionId(regionId);
        tWiringDiagram.setRegionName(regionName);
        String fileAbsPath = (String) redisTemplate.opsForHash().get("t_sys_param:fileAbsPath", "content");
        String fileRealPath = (String) redisTemplate.opsForHash().get("t_sys_param:fileRealPath", "content");
        String folderName = "wiringDiagram" + "/" +  regionName + "/" + DateTimeUtil.format3(new Date()) + "/";
        String fileRealPathTemp = fileRealPath + folderName + file.getOriginalFilename();
        tWiringDiagram.setPicPath(fileRealPathTemp);

        BufferedImage read;
        try {
            read = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            result.setMessage(209, "图片损坏或不符合要求");
            return result;
        }
        // 保存文件
        String fileAbsPathTemp = fileAbsPath + folderName + file.getOriginalFilename();
        try {
            FileUtil.saveFile(file, fileAbsPathTemp);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage(209, "图片保存失败");
            return result;
        }

        tWiringDiagram.setPicLength(read.getHeight());
        tWiringDiagram.setPicWidth(read.getWidth());
        tWiringDiagram.setUploadTime(new Date());
        String userName = (String) redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
        tWiringDiagram.setUploadPerson(userName);
        tWiringDiagram.setDeleteFlag(0);
        result.setData(tWiringDiagramDao.insert(tWiringDiagram));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer wiringDiagramId, Long userId) {
        TWiringDiagram tWiringDiagram = selectByPrimaryId(wiringDiagramId);
        tWiringDiagram.setDeleteFlag(1);
        tWiringDiagram.setUpdateTime(new Date());
        String userName = (String) redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
        tWiringDiagram.setUpdatePerson(userName);
        // 逻辑删除
        tWiringDiagramDao.update(tWiringDiagram);
        // 删除与设备的关联关系
        return tWiringConfigService.deleteByWiringDiagramId(wiringDiagramId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result update(MultipartFile file, Long regionId, String regionName, Long userId, Result result) {
        if (file == null) {
            result.setMessage(209, "文件错误,请重试");
            return result;
        }
        if (-1 == regionId) {
            TStdRegion tStdRegion = tStdRegionDao.selectRootRegion();
            regionId = tStdRegion.getRegionId();
            regionName = tStdRegion.getRegionName();
        }
        TWiringDiagram tWiringDiagramTemp = selectByCondition(regionId);
        tWiringDiagramTemp.setRegionId(regionId);
        tWiringDiagramTemp.setRegionName(regionName);
        String fileAbsPath = (String) redisTemplate.opsForHash().get("t_sys_param:fileAbsPath", "content");
        String fileRealPath = (String) redisTemplate.opsForHash().get("t_sys_param:fileRealPath", "content");
        String folderName = "wiringDiagram" + "/" +  regionName + "/" + DateTimeUtil.format3(new Date()) + "/";
        String fileRealPathTemp = fileRealPath + folderName + file.getOriginalFilename();
        tWiringDiagramTemp.setPicPath(fileRealPathTemp);
        BufferedImage read;
        try {
            read = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            result.setMessage(209, "图片损坏或不符合要求");
            return result;
        }

        // 保存文件
        String fileAbsPathTemp = fileAbsPath + folderName + file.getOriginalFilename();
        try {
            FileUtil.saveFile(file, fileAbsPathTemp);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setMessage(209, "图片保存失败");
            return result;
        }
        tWiringDiagramTemp.setPicLength(read.getHeight());
        tWiringDiagramTemp.setPicWidth(read.getWidth());
        tWiringDiagramTemp.setUpdateTime(new Date());
        String userName = (String) redisTemplate.opsForHash().get("userInfo:" + userId, "userName");
        tWiringDiagramTemp.setUpdatePerson(userName);
        result.setData(tWiringDiagramDao.update(tWiringDiagramTemp));
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public TWiringDiagram selectByPrimaryId(Integer wiringDiagramId) {
        return this.tWiringDiagramDao.selectByPrimaryId(wiringDiagramId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TWiringDiagram selectByCondition(Long regionId) {
        if (-1 == regionId) {
            TStdRegion tStdRegion = tStdRegionDao.selectRootRegion();
            regionId = tStdRegion.getRegionId();
        }
        return this.tWiringDiagramDao.selectByCondition(regionId);
    }

}
