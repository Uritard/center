package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.dao.SysUserBackUpDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author tt
 * @since 2020-07-23
 */
@Service
public class SysUserBackUpService {

        @Resource
        private SysUserBackUpDao sysUserBackUpDao;

        private Logger log = LoggerFactory.getLogger(this.getClass());




}

