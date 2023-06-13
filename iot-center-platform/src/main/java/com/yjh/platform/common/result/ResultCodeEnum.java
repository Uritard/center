package com.yjh.platform.common.result;

/**
 * @author tt
 * @ClassName: ResultCodeEnum
 * @Description: 自定义错误异常码
 * @date 2018/5/16 16:29
 */
public enum ResultCodeEnum {

    SYSTEMERROR(500, "系统异常"),
    DELETESUCCESS(204, "用户删除数据成功"),
    NORMAL(200, "正常"),
    UNAUTHORIZED(401, "用户没有登录，或没有accessToken"),
    CREATEORUPDATEERROR(201, "新建或修改数据失败"),
    DELETEERROR(206, "删除数据失败"),
    QUERYERROR(208, "数据查询失败"),
    UPDATEERROR(209, "更新数据失败"),
    RUNTASKFAILURE(213, "运行任务失败"),
    INVALIDREQUEST(400, "用户发出的请求有错误，服务器没有进行新建或修改数据的操作"),
    ACCEPTED(202, "表示一个请求已经进入后台排队（异步任务）"),
    FORBIDDEN(403, "用户得到授权（与401错误相对），但是访问是被禁止的"),
    NOTFOUND(404, "请求针对的是不存在的记录，服务器没有进行操作"),
    NOTACCEPTABLE(406, "请求的格式不可得"),
    PARAMERROR(407, "请求参数错误"),
    GONE(410, "用户请求的资源被永久删除，且不会再得到的"),
    UNPROCESABLEENTITY(422, "创建一个对象时，发生一个验证错误"),
    FILEUPLOADERROR(600, "文件上传异常"),
    UNSUPPORTFILETYPE(601, "不支持的文件格式"),
    FILEDELETED(602, "您要下载的资源已被删除！！"),
    CODE0(0, "成功"),
    CODE1(1, "未知错误"),
    CODE2(2,"添加失败"),

    CODE10001(10001, "服务暂时不可用"),
    CODE10002(10002, "请求来自未经授权的IP地址"),
    CODE10003(10003, "无权限访问该用户数据"),
    CODE10004(10004, "来自该refer的请求无访问权限"),
    CODE10005(10005, "请求参数无效"),
    CODE10006(10006, "请求参数过多"),
    CODE10007(10007, "无效的ip参数"),
    CODE10008(10008, "用户权限不足"),
    CODE10009(10009, "无效的操作方法"),
    CODE10010(10010, "指定的对象不存在"),
    CODE10011(10011, "指定的对象已存在"),
    CODE10012(10012, "数据库操作出错，请重试"),
    CODE10013(10013, "日期格式错误"),
    CODE10014(10014, "图片格式错误"),
    CODE10015(10015, "附件格式错误"),
    CODE10016(10016, "数据库返回数据有误"),
    CODE10017(10017, "测点不存在"),
    CODE10018(10018, "日期范围无效"),
    CODE10019(10019, "日期范围不能大于7天"),
    CODE10101(10101, "用户名或者密码错误"),
    CODE10102(10102, "用户已锁定"),
    CODE10103(10103, "用户名或密码为空"),
    CODE10104(10104, "机器人ip格式不正确"),
    CODE10105(10105, "机器人端口号格式不正确"),
    CODE10106(10106, "密码错误"),
    CODE10107(10107, "用户已登陆"),
    CODE10108(10108, "密码超期登录失败，请联系管理员处理！"),
    CODE10109(10109, "当前用户无权限添加或修改敏感字段"),
    CODE20017(20017, "参数规则不匹配"),
    CODE20018(20018, "导入表格缺少字段");

    private int code;
    private String name;

    private ResultCodeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static ResultCodeEnum getNameByCode(int code) {
        for (ResultCodeEnum resultCodeEnum : ResultCodeEnum.values()) {
            if (code == resultCodeEnum.getCode()) {
                return resultCodeEnum;
            }
        }
        return null;
    }
}