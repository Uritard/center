package com.yjh.platform.module.user.controller;

import com.alibaba.fastjson.JSONArray;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.module.user.service.SysOrgService;
import com.yjh.platform.module.user.entity.SysOrg;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;

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
    public Result insert(@RequestBody SysOrg sysOrg) {
        Result result = new Result();
        try {
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
    public Result delete(@RequestParam(value = "orgId", required = true) Long orgId) {
        Result result = new Result();
        try {
            result.setData(sysOrgService.deleteByPrimaryId(orgId));
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
    public Result selectByPage(@RequestBody SysOrg sysOrg,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
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
    @RequestMapping(value = "/selectOrgTreeByOrgName", method = RequestMethod.GET)
    public Result selectOrgTreeByOrgName(@RequestParam(value = "orgName", required = false) String orgName) {
        Result result = new Result();
        try {
            result.setData(sysOrgService.selectOrgTreeByOrgName(orgName));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
