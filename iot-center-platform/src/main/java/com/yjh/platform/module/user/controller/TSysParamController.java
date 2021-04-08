package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.service.TSysParamService;
import com.yjh.platform.module.user.entity.TSysParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.result.Result;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;

import java.util.Map;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * @author tt
 * @since 2020-08-07
 */
@RestController
@RequestMapping("/tSysParam/v1")
@Api(value = "/tSysParam", description = "系统参数表操作接口")
public class TSysParamController {

    @Autowired
    private final TSysParamService tSysParamService;
    @Autowired
    private SysUserDao sysUserDao;
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TSysParamController.class);

    public TSysParamController(TSysParamService tSysParamService) {
        this.tSysParamService = tSysParamService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增系统参数", content = "根据用户传递的参数新增系统参数", logType = 2)
    public Result insert(@Validated @RequestBody TSysParam tSysParam) {

        Result result = new Result();
        try {
            result.setData(tSysParamService.insert(tSysParam));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加系统参数错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除系统参数", content = "根据用户传递的参数删除系统参数", logType = 4)
    public Result delete(@RequestParam(value = "paramId", required = true) Integer paramId) {
        Result result = new Result();
        try {
            result.setData(tSysParamService.deleteByPrimaryId(paramId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除系统参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除系统参数错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改系统参数", content = "根据用户传递的参数修改系统参数", logType = 3)
    public Result update(@RequestBody TSysParam tSysParam, HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String secureVerify = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify", "content"));
            String[] secures = secureVerify.split(",");
            if (tSysParam.getParamCode().equals("cpuFreeMin")) {
                for (String memory : secures) {
                    if (memory.equals("cpuFreeMin")) {
                        if (userId != 10001) {
                            result.setCode(209, "当前用户无权限修改cpu信息");
                            return result;
                        }
                    }
                }
            }
            if (tSysParam.getParamCode().equals("DiskFreeMin")) {
                for (String memory : secures) {
                    if (memory.equals("DiskFreeMin")) {
                        if (userId != 10001) {
                            result.setCode(209, "当前用户无权限修改磁盘信息");
                            return result;
                        }
                    }
                }
            }
            if (tSysParam.getParamCode().equals("MemoryFreeMin")) {
                for (String memory : secures) {
                    if (memory.equals("MemoryFreeMin")) {
                        if (userId != 10001) {
                            result.setCode(209, "当前用户无权限修改内存信息");
                            return result;
                        }
                    }
                }
            }
            result.setData(tSysParamService.update(tSysParam));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新系统参数异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新系统参数错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询系统参数", content = "根据用户传递的参数查询系统参数", logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "paramId", required = true) Integer paramId) {
        Result result = new Result();
        try {
            TSysParam tSysParam = tSysParamService.selectByPrimaryId(paramId);
            result.setData(tSysParam);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统参数查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询系统参数", content = "根据用户传递的参数查询系统参数", logType = 1)
    public Result select(@RequestParam(value = "paramId", required = false) Integer paramId,
                         @RequestParam(value = "paramCode", required = false) String paramCode,
                         @RequestParam(value = "paramType", required = false) String paramType,
                         @RequestParam(value = "paramName", required = false) String paramName,
                         @RequestParam(value = "content", required = false) String content,
                         @RequestParam(value = "remark", required = false) String remark) {
        Result result = new Result();
        try {

            List<TSysParam> list = tSysParamService.select(paramId, paramCode, paramType, paramName, content, remark);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统参数查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询系统参数", content = "根据用户传递的参数分页查询系统参数", logType = 1)
    public Result selectByPage(@RequestParam(value = "paramId", required = false) Integer paramId,
                               @RequestParam(value = "paramCode", required = false) String paramCode,
                               @RequestParam(value = "paramType", required = false) String paramType,
                               @RequestParam(value = "paramName", required = false) String paramName,
                               @RequestParam(value = "content", required = false) String content,
                               @RequestParam(value = "remark", required = false) String remark,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
            List<TSysParam> list = tSysParamService.selectByPage(paramId, paramCode, paramType, paramName, content, remark);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统参数分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchInsert", method = RequestMethod.POST)
    @Logs(title = "批量插入系统参数", content = "根据用户传递的参数批量插入系统参数", logType = 2)
    public Result batchInsert(@RequestBody List<TSysParam> list) {
        Result result = new Result();
        try {
            result.setData(tSysParamService.batchInsert(list));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("系统参数批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "将数据写入redis")
    @RequestMapping(value = "/insertIntoRedis", method = RequestMethod.GET)
    public Result insertIntoRedis() {
        Result result = new Result();
        try {
            result.setData(this.tSysParamService.insertIntoRedis());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("将数据写入redis失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询系统参数")
    @RequestMapping(value = "/selectQuery", method = RequestMethod.GET)
    @Logs(title = "查询系统参数", content = "根据用户传递的参数分页查询系统参数", logType = 1)
    public Result selectQuery(@RequestParam(value = "params") String params) {
        Result result = new Result();
        try {
            String[] splitColNames = params.split("'|,");
            List<String> list = new ArrayList<>();
            for (String ColName : splitColNames) {
                list.add(ColName);
            }
            result.setData(tSysParamService.selectQuery(list));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统参数分页查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询系统参数")
    @RequestMapping(value = "/updateByCode", method = RequestMethod.GET)
    @Logs(title = "查询系统参数", content = "根据用户传递的参数分页查询系统参数", logType = 1)
    public Result updateByCode(HttpServletRequest request,
                               @RequestParam(value = "paramCode") String paramCode,
                               @RequestParam(value = "content") String content) {
        Result result = new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            SysUser sysUser = sysUserDao.selectByPrimaryId(userId);
            if (sysUser.getRoleId() != null && sysUser.getRoleId() == 1234) {
                result.setData(tSysParamService.updateByCode(paramCode, content));
            } else {
                result.setCode(10008, "用户权限不足");
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("系统参数分页查询失败描述：", e);
        }
        return result;
    }
}
