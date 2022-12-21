package com.yjh.platform.module.user.service;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.configuration.SysParamConfig;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TSysParam;
import com.yjh.platform.module.user.entity.Version;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-08-07
*/
@Service
@Slf4j
public class TSysParamService{

    @Autowired
    private TSysParamDao tSysParamDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SysParamConfig sysParamConfig;


    @Transactional(rollbackFor = Exception.class)
    public int insert(TSysParam tSysParam) {
         this.tSysParamDao.insert(tSysParam);
         return this.insertIntoRedis();
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Integer paramId) {
        this.tSysParamDao.deleteByPrimaryId(paramId);
        return this.insertIntoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TSysParam tSysParam) {
        this.tSysParamDao.update(tSysParam);
        return this.insertIntoRedis();
    }

    /**
     * 校验安全参数规则
     */
    public boolean secureVerify(TSysParam tSysParam, String userId, Result result) {
        Map<String, String> appKeymap = redisTemplate.opsForHash().entries("t_sys_param:secureVerify");
        String secureVerify = appKeymap.get("content");
        String ruleStr = appKeymap.get("remark");
        // 判断开关是否打开
        /*if (!"true".equals(secureVerify)) {
           return true;
        }*/

        String paramCode = tSysParam.getParamCode();
        String sysRules = (String)redisTemplate.opsForHash().get("t_sys_param:" + paramCode, "rules");
        if (StringUtils.isNotEmpty(sysRules)) {
            try {
                JSONObject ruleObj = JSON.parseObject(sysRules);
                String prule = ruleObj.getString("rule");
                String pmsg = ruleObj.getString("msg");
                if (!StringUtils.isAnyEmpty(prule, pmsg) && !tSysParam.getContent().matches(prule)) {
                    result.setCode(209, pmsg);
                    return false;
                }
            } catch (Exception e) {
                log.error("{} 配置规则不正确，不是 JSON 格式，rules: {}", paramCode, sysRules);
            }
        }

        if ("secureVerify".equals(paramCode) && !"10001".equals(userId)) {
            result.setCode(209, "仅系统默认管理员可修改[安全标记]");
            return false;
        } else if ("secureVerify".equals(paramCode)) {
            // 如果是安全标记项，且是默认管理员，则可以修改此参数，不需向后校验
            return true;
        }
        // 获取用户名
        String userName = (String) redisTemplate.opsForHash().entries("userInfo:"+userId).get("userName");
        String[] rules = StringUtils.split(ruleStr, "\n");
        boolean verified = true;
        boolean  selfChecked = false;

        outside:
        for (String rule : rules){
            // 解析用户匹配规则 manager:MemoryFreeMin,cpuFreeMin,DiskFreeMin,!loginKeepByWs
            int spIdx = StringUtils.indexOf(rule, ":");
            if(spIdx <= 0 || spIdx >= rule.length() - 1){
                log.warn("规则不符合规范或为空，rule: {}, spIdx: {}", rule, spIdx);
                // 具体规则为空
                continue;
            }
            String[] kvRule = rule.split(":", 2);
            // 规则匹配用户名
            String name = kvRule[0].trim();
            // 对应匹配规则
            String[] rls = kvRule[1].split(",");
            // 是否当前用户
            boolean userSelf = Objects.equals(userName, name);
            // 去除空行和空格
            List<String> codes = Arrays.stream(rls).filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
            if(userSelf) {
                selfChecked = true;
            }
            for (String code : codes){
                // 匹配非运算
                boolean isNot = code.charAt(0) == '!';
                if(isNot){
                    code = code.substring(1);
                }
                if(userSelf){
                    // 当前用户的规则如果匹配上，则判断是否非运算，然后直接跳出最外层循坏，否则继续循坏
                    if(Objects.equals(code, paramCode)){
                        verified = !isNot;
                        break outside;
                    }
                } else {
                    // 非当前用户，如果匹配上，则表明此规则被其他用户占用，当前规则结束，继续匹配下一条规则
                    if(Objects.equals(code, paramCode) && !isNot){
                        verified = false;
                        if(selfChecked){
                            // 如果已经匹配过
                            break outside;
                        }
                        break;
                    }
                }
            }
            if(selfChecked && !verified){
                // 如果规则已被分配给其他用户，且当前用户无法匹配，则表示此规则不需要再匹配，直接跳出
                break;
            }
        }
        if(!verified){
            result.setCode(209, "当前用户无权限修改[" + tSysParam.getParamName() + "]");
        }
        return verified;
    }

    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryId(Integer paramId) {
        return this.tSysParamDao.selectByPrimaryId(paramId);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> select(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.select(paramId,paramCode,paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Transactional(rollbackFor = Exception.class)
    public List<TSysParam> selectByPage(Integer paramId,String paramCode, String paramType, String paramName, String content, String remark) {
        List<TSysParam> tSysParamList = tSysParamDao.selectByPage(paramId,paramCode, paramType, paramName, content, remark);
        return tSysParamList;
    }


    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TSysParam> list) {
        this.tSysParamDao.batchInsert(list);
        return this.insertIntoRedis();
    }

    public int insertIntoRedis(){
        return insertIntoRedis(false);
    }

    public int insertIntoRedis(boolean putSysProp){
        List<TSysParam> list = this.tSysParamDao.selectAll();
        for (TSysParam item:list) {
            Map map = Object2Map.objectToMap(item,true);
            String str = "t_sys_param:"+item.getParamCode();
            redisTemplate.opsForHash().putAll(str, map);
        }
        if(putSysProp) {
            log.info("load secure param to redis...");
            sysParamConfig.putToRedis();
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> selectQuery(List<String> params) {
        List<TSysParam> list = this.tSysParamDao.selectQuery(params);
        Map<String,Object> mapFOrRe = new HashMap<>();
        for (TSysParam item: list){
            mapFOrRe.put(item.getParamCode(),item.getContent());
        }
        return mapFOrRe;
    }


    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByPrimaryCode() {
        return this.tSysParamDao.selectByPrimaryCode();
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateByCode(String paramCode,String content){
         this.tSysParamDao.updateByCode(paramCode,content);
        return this.insertIntoRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public TSysParam selectByParamCode(String paramType) {
        return this.tSysParamDao.selectByParamType(paramType);
    }

    public Map<String,String> sysConfig(){
        Map<String,String> result = Maps.newHashMap();
        if (MapUtil.isNotEmpty(sysParamConfig.getSecure())) {
            result.put("isEncryption", sysParamConfig.getSecure().get("isEncryption"));
            result.put("isUkey", sysParamConfig.getSecure().get("isUkey"));
            result.put("summaryFlag", sysParamConfig.getSecure().get("summaryFlag"));
        }
        return result;
    }

    public List<Version> selectVersion(int type){
        return tSysParamDao.selectVersion(type);
    }
}

