package com.yjh.platform.module.video.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.video.convert.CameraPlayLogConverter;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogDTO;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogQueryReq;
import com.yjh.platform.module.video.entity.cameralog.CameraPlayLogVO;
import com.yjh.platform.module.video.enums.CameraRecordStatusEnum;
import com.yjh.platform.module.video.service.CameraPlayLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangyuyi
 * @create 2023-11-03
 * 相机播放日志*
 */
@RestController
@RequestMapping("/cameraPlayLog/v1")
@Api(value = "/cameraPlayLogControl", tags = {"相机播放日志"})
public class CameraPlayLogController {

    @Resource
    private CameraPlayLogService cameraPlayLogService;

    @ApiOperation(value = "查询播放记录")
    @RequestMapping(value = "/queryCameraPlayLogForPage", method = RequestMethod.POST)
    public Result queryCameraPlayLogForPage(@RequestBody CameraPlayLogVO cameraPlayLogVO) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            CameraPlayLogDTO cameraPlayLogDTO = CameraPlayLogConverter.convertVO2DTO(cameraPlayLogVO);
            Page page = PageHelper.startPage(cameraPlayLogVO.getPageNum(), cameraPlayLogVO.getPageSize(),
                    true, null, true);
            List<CameraPlayLogDTO> dtoList = cameraPlayLogService.queryCameraPlayLog(cameraPlayLogDTO);
            if (CollectionUtils.isEmpty(dtoList)) {
                result.setData(new ArrayList());
            } else {
                resultMap.put("count", page.getTotal());
                resultMap.put("list", dtoList.stream().map(e ->
                        CameraPlayLogConverter.convertDTO2VO(e)).collect(Collectors.toList()));
                result.setData(resultMap);
            }
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "记录播放记录")
    @RequestMapping(value = "/recordVideoPlayLog", method = RequestMethod.POST)
    public Result recordVideoPlayLog(@RequestBody CameraPlayLogVO cameraPlayLogVO) {
        Result result = new Result();
        try {
            CameraPlayLogDTO cameraPlayLogDTO = CameraPlayLogConverter.convertVO2DTO(cameraPlayLogVO);
            if (CameraRecordStatusEnum.START.getCode().equals(cameraPlayLogVO.getStatus())) {
                cameraPlayLogService.addStartLog(cameraPlayLogDTO);
            } else {
                cameraPlayLogService.updateStopLog(cameraPlayLogDTO);
            }
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
