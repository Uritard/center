package com.yjh.platform.module.user.service;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.RedisAndYxsjUtil;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.dao.SysRoleMenuDao;
import com.yjh.platform.module.user.dao.SysUserBackUpDao;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.SysUserBackUp;
import com.yjh.platform.module.user.entity.SysUserLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

