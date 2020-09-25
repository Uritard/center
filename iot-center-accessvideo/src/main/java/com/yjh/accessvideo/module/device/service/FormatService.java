package com.yjh.accessvideo.module.device.service;

import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.module.device.dao.FormatDao;
import com.yjh.accessvideo.module.device.entity.Format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class FormatService{

    @Autowired
    private FormatDao formatDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(Format format) {
        return this.formatDao.insert(format);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String testId) {
        return this.formatDao.deleteByPrimaryId(testId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(Format format) {
        return this.formatDao.update(format);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Format selectByPrimaryId(String testId) {
        return this.formatDao.selectByPrimaryId(testId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Format> select(String testId, String testType) {
        List<Format> formatList = formatDao.select(testId, testType);
        return formatList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Format> selectByPage(Format format) {
        List<Format> formatList = formatDao.selectByPage(format);
        return formatList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<Format> list) {
        return this.formatDao.batchInsert(list);
    }

}

