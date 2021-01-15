package com.yjh.accessvqd.common.utils;


/**
 * @ClassName:   EnumLog
 * @Description: 自定义常量
 * @author lilingling
 *
 */
public interface EnumLog {

    /*********** 日志大类 **************/
    int OBJECT_KIND_DEVICE = 1;
    int OBJECT_KIND_USER = 2;
    int OBJECT_KIND_REGION = 3;
    int OBJECT_KIND_MODULE = 4;
    int OBJECT_KIND_ROLE = 5;

    /*********** operation_kind 0 操作日志 下的 日志小类 **************/
    int OPERATION_ADD = 1;
    int OPERATION_DEL = 2;
    int OPERATION_UPDATE = 3;
    int OPERATION_QUERY = 4;
    int OPERATION_LOGIN = 5;
    int OPERATION_SYNC = 6;
    int OPERATION_AUTH = 7;

    /**** operation_kind 1 导入导出 下的 operation*********/
    int OPERATION_IMPORT_CAR = 9;
    int OPERATION_IMPORT_PRECINCT = 10;
    int OPERATION_IMPORT_COMMONPERSON = 11;
    //警情
    int OPERATION_IMPORT_PLSITUATION = 12;
    //警力
    int OPERATION_IMPORT_PLPOWER = 13;
    //群防群治
    int OPERATION_IMPORT_QFQZ = 14;
    int OPERATION_IMPORT_WATER = 15;
    int OPERATION_IMPORT_ELECPOWER = 16;
    int OPERATION_IMPORT_GAS = 17;
    int OPERATION_IMPORT_OLDPEOPLE = 18;
    int OPERATION_IMPORT_COMPANY = 19;


    /**** operation_kind 2 接口日志 下的 operation*********/
    int OPERATION_METHOD_GET = 20;
    int OPERATION_METHOD_POST = 21;
    int OPERATION_METHOD_PATCH = 22;
    int OPERATION_METHOD_DELETE = 23;
    int OPERATION_METHOD_PUT = 24;

    /********* 字符串常量 ***********/
    String   SUCCESS = "0";
    String   FAILURE = "1";
    String   UNKNOWN = "2";


    /***********日志类型*************/
    /**
     * 操作日志
     */
    int LOG_OPKIND_OPERATION = 0;
    /**
     * 导入导出日志
     */
    int LOG_OPKIND_IMOUTPORT = 1;
    /**
     * 接口调用日志
     */
    int LOG_OPKIND_INTERFACE = 2;
}