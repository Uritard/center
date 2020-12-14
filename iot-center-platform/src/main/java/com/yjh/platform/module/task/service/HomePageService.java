package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCameraAlarm;
import com.yjh.platform.module.task.entity.WarnStatistical;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lqh
 * @since 2020/12/14
 */
@Service
public class HomePageService {

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;

    @Logs(title = "巡视任务数据", code = "TaskForHomeService",content = "根据页面参数查询任务数据概览")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> taskInfo(Integer date) {
        if(1 == date){
            return tCruiseTaskDao.selectOnWeek();
        }
        if(2 == date){
            return tCruiseTaskDao.selectOnMonth();
        }
        if(3 == date){
            return tCruiseTaskDao.selectOnYear();
        }
        if(4 == date){
            return tCruiseTaskDao.selectForSevenDay();
        }
        if(5 == date){
            return tCruiseTaskDao.selectForMonth();
        }
        if(6 == date){
            return tCruiseTaskDao.selectForYear();
        }
        return null;
    }
}
