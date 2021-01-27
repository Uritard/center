package com.yjh.platform.common.logs;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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
     * 日志状态:1-操作成功;2-操作失败;
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

    public static OperateLogDto build(String title, Integer logType, Integer state, String content){
        return new OperateLogDto(title, logType, state, content);
    }

    public static OperateLogDto buildSuccessLog(Logs logAnnotation){
        return build(logAnnotation.title(), logAnnotation.logType(), OPERATE_RESULT_SUCCESS, logAnnotation.content());
    }

    public static OperateLogDto buildSuccessLog(String title, Integer logType){
        return build(title, logType, OPERATE_RESULT_SUCCESS, null);
    }

    public static OperateLogDto buildSuccessLog(String title, Integer logType, String content){
        return build(title, logType, OPERATE_RESULT_SUCCESS, content);
    }

    public static OperateLogDto buildFailedLog(Logs logAnnotation){
        return build(logAnnotation.title(), logAnnotation.logType(), OPERATE_RESULT_FAILED, logAnnotation.content());
    }

    public static OperateLogDto buildFailedLog(String title, Integer logType){
        return build(title, logType, OPERATE_RESULT_FAILED, null);
    }

    public static OperateLogDto buildFailedLog(String title, Integer logType, String content){
        return build(title, logType, OPERATE_RESULT_FAILED, content);
    }

    public static OperateLogDto buildExceptionLog(Logs logAnnotation){
        return build(logAnnotation.title(), logAnnotation.logType(), OPERATE_RESULT_EXCEPTION, logAnnotation.content());
    }

    public static OperateLogDto buildExceptionLog(String title, Integer logType){
        return build(title, logType, OPERATE_RESULT_EXCEPTION, null);
    }

    public static OperateLogDto buildExceptionLog(String title, Integer logType, String content){
        return build(title, logType, OPERATE_RESULT_EXCEPTION, content);
    }
}
