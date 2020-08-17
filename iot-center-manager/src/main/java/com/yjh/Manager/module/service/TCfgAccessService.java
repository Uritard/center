package com.yjh.Manager.module.service;

import java.util.List;
import java.util.Date;

import com.yjh.Manager.logs.Logs;
import com.yjh.Manager.module.dao.TCfgAccessDao;
import com.yjh.Manager.module.entity.TCfgAccess;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-17
*/
@Service
public class TCfgAccessService{

    @Autowired
    private TCfgAccessDao tCfgAccessDao;

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAccess> select(Long accessId, String name, Long projectId, String url, String port, String appId, String secret, String accessCode, Long userId, String userName, Date createTime, Date updateTime, String remark, String state) {
        List<TCfgAccess> tCfgAccessList = tCfgAccessDao.select(accessId, name, projectId, url, port, appId, secret, accessCode, userId, userName, createTime, updateTime, remark, state);
        return tCfgAccessList;
    }

}

