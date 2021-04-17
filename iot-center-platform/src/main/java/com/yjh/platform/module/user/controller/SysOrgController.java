package com.yjh.platform.module.user.controller;

import com.alibaba.fastjson.JSONArray;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysOrgService;
import com.yjh.platform.module.user.entity.SysOrg;

import java.util.*;

import io.swagger.annotations.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import com.yjh.platform.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;


/**
 * @author tt
 * @since 2020-07-24
 */
@RestController
@RequestMapping("/sysOrg/v1")
@Api(value = "/sysOrg", description = "组织机构表操作接口")
public class SysOrgController {

    @Autowired
    private final SysOrgService sysOrgService;

    private Logger log = LoggerFactory.getLogger(SysOrgController.class);

    public SysOrgController(SysOrgService sysOrgService) {
        this.sysOrgService = sysOrgService;
    }

    @ApiOperation(value = "新增组织机构")
    @RequestMapping(value = "/addOrg", method = RequestMethod.POST)
    @Logs(title = "新增组织机构",content = "根据用户传递的参数新增组织机构数据",logType = 2,authority = "1234")
    public Result insert(@Validated @RequestBody   SysOrg sysOrg) {

        Result result = new Result();
        try {
            if (sysOrg.getOrgCode().equals("")) {
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), "组织机构编码为空");
                return result;
            }
            if (sysOrgService.judgeOrgCode(sysOrg.getOrgCode())) {
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), "组织机构编码重复");
                return result;
            }
            result.setData(sysOrgService.insert(sysOrg));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加组织机构错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除组织机构",content = "根据用户传递的参数删除组织机构数据",logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "orgId", required = true) Long orgId) {
        Result result = new Result();
        try {
            int re  = sysOrgService.deleteByPrimaryId(orgId);
            if(re == -1){
                result.setCode(209,"此组织下存在子组织");
            }else {
                result.setData(re);
            }

        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除组织机构异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除组织机构错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改组织机构",content = "根据用户传递的参数修改组织机构数据",logType = 3,authority = "1234")
    public Result update(@RequestBody SysOrg sysOrg) {
        Result result = new Result();
        try {
            result.setData(sysOrgService.update(sysOrg));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新组织机构异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新组织机构错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询组织机构",content = "根据用户传递的参数查询组织机构信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "orgId", required = true) Long orgId) {
        Result result = new Result();
        try {
            SysOrg sysOrg = sysOrgService.selectByPrimaryId(orgId);
            result.setData(sysOrg);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询组织机构",content = "根据用户传递的参数查询组织机构信息",logType = 1)
    public Result select(@RequestParam(value = "orgId", required = false) Long orgId,
                         @RequestParam(value = "orgName", required = false) String orgName,
                         @RequestParam(value = "orgCode", required = false) String orgCode,
                         @RequestParam(value = "upId", required = false) Long upId,
                         @RequestParam(value = "sort", required = false) Integer sort,
                         @RequestParam(value = "createTime", required = false) Date createTime,
                         @RequestParam(value = "creatorId", required = false) Long creatorId,
                         @RequestParam(value = "orgLevel", required = false) Integer orgLevel,
                         @RequestParam(value = "orgPath", required = false) String orgPath,
                         @RequestParam(value = "deptName", required = false) String deptName) {
        Result result = new Result();
        try {
            List<SysOrg> list = sysOrgService.select(orgId, orgName, orgCode, upId, sort, createTime, creatorId, orgLevel, orgPath, deptName);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    @Logs(title = "查询组织机构",content = "根据用户传递的参数分页查询组织机构信息",logType = 1)
    public Result selectByPage(@RequestBody SysOrg sysOrg
//                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
//                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize
    ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            //Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            Page page = PageHelper.startPage(sysOrg.getPageNum()!=null?sysOrg.getPageNum():1, sysOrg.getPageSize()!=null?sysOrg.getPageSize():0, true, null, true);
            List<SysOrg> list = sysOrgService.selectByPage(sysOrg);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "组织机构树查询")
    @RequestMapping(value = "/selectOrgTree", method = RequestMethod.GET)
    @Logs(title = "组织机构树查询",content = "查询组织区域树",logType = 1,authority = "1234")
    public Result selectOrgTree() {
        Result result = new Result();
        try {
            result.setData(sysOrgService.selectOrgTree());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据组织机构名称模糊查询")
    @RequestMapping(value = "/selectOrgTreeByName", method = RequestMethod.GET)
    @Logs(title = "根据组织机构名称模糊查询",content = "根据用户传递的参数查询组织区域树",logType = 1,authority = "1234")
    public Result selectOrgTreeByName(@RequestParam(value = "orgName", required = false) String orgName) {
        Result result = new Result();
        try {
            result.setData(sysOrgService.selectOrgTreeByName(orgName));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
