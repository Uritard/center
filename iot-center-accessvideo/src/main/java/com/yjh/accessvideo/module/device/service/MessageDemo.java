package com.yjh.accessvideo.module.device.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.weaver.ast.Var;

import java.util.*;

/**
 * @author 丫C
 * @date 2022/4/11
 */
public class MessageDemo {

    public static void main(String[] args) {
        picAnalyseMsg();

        algorithmUpdateMsg();
    }

    private static void picAnalyseMsg() {
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", "192.168.10.2");
        param.put("requestHostPort", "1234");
        param.put("requestId", String.valueOf(UUID.randomUUID()));

        List<Map<String, Object>> objectList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>(16);
        map.put("objectId","123");
        map.put("imageNormalUrlPath","123.jpg");
        List<String> typeList = new ArrayList<>();
        typeList.add("bj_bpmh");
        typeList.add("bj_bpps");
        map.put("typeList",typeList);
        List<String> imageUrlList = new ArrayList<>();
        imageUrlList.add("jpg1图像");
        imageUrlList.add("jpg2图像");
        map.put("imageUrlList",imageUrlList);
        objectList.add(map);

        Map<String, Object> map2 = new HashMap<>(16);
        map2.put("objectId","123");
        map2.put("imageNormalUrlPath","123.jpg");
        List<String> typeList2 = new ArrayList<>();
        typeList2.add("bj_bpmh");
        typeList2.add("bj_bpps");
        map2.put("typeList",typeList2);
        List<String> imageUrlList2 = new ArrayList<>();
        imageUrlList2.add("jpg1图像");
        imageUrlList2.add("jpg2图像");
        map2.put("imageUrlList",imageUrlList2);
        objectList.add(map2);

        param.put("objectList", objectList);

        printJsonMsg(param);
    }

    private static void algorithmUpdateMsg() {
        Map<String, Object> param = new HashMap<>(16);
        param.put("requestHostIp", "192.168.10.2");
        param.put("requestHostPort", "1234");
        param.put("requestId", String.valueOf(UUID.randomUUID()));
        param.put("algorithmPath", "/home/yjh/iot-center/xxx.zip");

        printJsonMsg(param);
    }

    private static void printJsonMsg(Map<String, Object> param) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(param);

        String pretty = JSON.toJSONString(jsonObject, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        System.out.println(pretty);

        System.out.println("====================================================================================");
    }
}
