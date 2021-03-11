package com.yjh.platform.module.device.controller;

import com.yjh.platform.module.device.service.TDeviceTypeImgService;
import com.yjh.platform.module.device.entity.TDeviceTypeImg;
import java.util.HashMap;
import java.util.List;

import io.swagger.annotations.*;
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


/**
 * @author lqh
 * @since 2021-03-11
 */
@RestController
@RequestMapping("/tDeviceTypeImg/v1")
@Api(value = "/tDeviceTypeImg", description = "操作接口")
public class TDeviceTypeImgController {

    @Autowired
    private final TDeviceTypeImgService tDeviceTypeImgService;

    private Logger log = LoggerFactory.getLogger(TDeviceTypeImgController.class);

    public TDeviceTypeImgController(TDeviceTypeImgService tDeviceTypeImgService) {
        this.tDeviceTypeImgService = tDeviceTypeImgService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result add(@RequestBody TDeviceTypeImg tDeviceTypeImg) {
        Result result = new Result();
        try {
            result.setData(tDeviceTypeImgService.add(tDeviceTypeImg));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(@RequestParam(value = "typeId", required = true) String typeId) {
        Result result = new Result();
        try {
            result.setData(tDeviceTypeImgService.deleteByPrimaryId(typeId));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody TDeviceTypeImg tDeviceTypeImg) {
        Result result = new Result();
        try {
            result.setData(tDeviceTypeImgService.update(tDeviceTypeImg));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(@RequestParam(value = "typeId", required = true) String typeId) {
        Result result = new Result();
        try {
            TDeviceTypeImg tDeviceTypeImg = tDeviceTypeImgService.selectByPrimaryId(typeId);
            result.setData(tDeviceTypeImg);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(@RequestParam(value = "typeId", required = false) String typeId,
                            @RequestParam(value = "picAbspath", required = false) String picAbspath,
                            @RequestParam(value = "picRealpath", required = false) String picRealpath,
                            @RequestParam(value = "remake", required = false) String remake) {
        Result result = new Result();
        try {
            List<TDeviceTypeImg> list = tDeviceTypeImgService.select(typeId, picAbspath, picRealpath, remake);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public Result selectByPage(@RequestParam(value = "typeId", required = false) String typeId,
                                @RequestParam(value = "picAbspath", required = false) String picAbspath,
                                @RequestParam(value = "picRealpath", required = false) String picRealpath,
                                @RequestParam(value = "remake", required = false) String remake,
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<TDeviceTypeImg> list = tDeviceTypeImgService.selectByPage(typeId, picAbspath, picRealpath, remake);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    public Result batchAdd(@RequestBody List<TDeviceTypeImg> list) {
        Result result = new Result();
        try {
        result.setData(tDeviceTypeImgService.batchAdd(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    public Result batchDelete(@RequestParam(value = "typeIds") String typeIds) {
    Result result = new Result();
    try {
        result.setData(tDeviceTypeImgService.batchDelete(typeIds));
    } catch (BusinessException e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除失败：" + e);
    }catch (Exception e) {
        result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量删除错误:", e);
    }
    return result;
    }

    @ApiOperation(value = "将图片同步到数据库中")
    @RequestMapping(value = "/findPic", method = RequestMethod.GET)
    public Result findPic() {
        Result result = new Result();
        try {
            result.setData(tDeviceTypeImgService.findPic());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("将图片同步到数据库中：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询设备类型和图片")
    @RequestMapping(value = "/selectDeviceTypeAndImg", method = RequestMethod.GET)
    public Result selectDeviceTypeAndImg() {
        Result result = new Result();
        try {
            result.setData(tDeviceTypeImgService.selectDeviceTypeAndImg());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("将图片同步到数据库中：" + e);
        }
        return result;
    }
}
