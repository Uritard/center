package com.yjh.logs.commons.logs;

import com.yjh.logs.common.context.UserContext;
import com.yjh.logs.common.context.UserContext.OperatorDto;
import com.yjh.logs.commons.logs.enums.OperateTypeEnum;
import com.yjh.logs.commons.logs.enums.RequestMethodEnum;
import com.yjh.logs.commons.logs.track.HttpTracing;
import com.yjh.logs.module.log.entity.SysOperateLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

/**
 * @author lichensi
 * @date 2020/12/10 14:00
 */

@Getter
@Setter
public class OperateLogDto {

    private static final int OPERATE_RESULT_SUCCESS = 1;

    private static final int OPERATE_RESULT_FAILED = 2;

    private static final int OPERATE_RESULT_EXCEPTION = 3;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String title;

    /**
     * 操作类型(1-查询;2-新增;3-修改;4-删除;5-执行)
     */
    @NotNull(message = "操作类型不能为空")
    private Integer logType;

    /**
     * 日志状态:1-操作成功;2-操作失败;3-操作异常;
     */
    private Integer state;

    /**
     * 操作内容
     */
    private String content;

    private OperateLogDto(String title, Integer logType, Integer state, String content){
        this.title = title;
        this.state = state;
        this.logType = logType;
        this.content = content;
    }

    public OperateLogDto() {}

    public SysOperateLog formatSysOpLog(){
        SysOperateLog sysOperateLog = new SysOperateLog();
        sysOperateLog.setTitle(title);
        sysOperateLog.setLogType(logType.toString());
        sysOperateLog.setState(state);
        if(StringUtils.isBlank(content)){
            sysOperateLog.setContent(OperateTypeEnum.assembleOperateContent(title, logType));
        } else {
            sysOperateLog.setContent(content);
        }
        sysOperateLog.setCreateTime(new Date());
        OperatorDto operatorDto = UserContext.getCurrentUser();
        if(Objects.nonNull(operatorDto)){
            sysOperateLog.setUserId(operatorDto.getUserId());
            sysOperateLog.setUserName(operatorDto.getName());
        }else {
            sysOperateLog.setUserId(99999999L);
            sysOperateLog.setUserName("SYSTEM");
        }
        sysOperateLog.setIp(HttpTracing.getRouteRequestIp());
        sysOperateLog.setRequestMethod(RequestMethodEnum.ofCode(HttpTracing.getRouteRequestMethod()));
        sysOperateLog.setRequestOrigin(HttpTracing.getRouteRequestOrigin());
        sysOperateLog.setRequestPath(HttpTracing.getRouteRequestPath());
        sysOperateLog.setTraceId(HttpTracing.getCurrentTraceID());
        return sysOperateLog;
    }
}
