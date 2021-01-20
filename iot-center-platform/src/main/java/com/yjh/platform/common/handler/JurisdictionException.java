package com.yjh.platform.common.handler;

public class JurisdictionException extends Exception{
     public JurisdictionException(){
         super("当前用户无权限添加或修改敏感字段");
     }
}
