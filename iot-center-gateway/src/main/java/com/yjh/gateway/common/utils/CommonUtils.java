package com.yjh.gateway.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陈玮
 * @version V1.0
 * @Package com.yjh.gateway.common.utils
 * @date 2019/12/30 9:43
 * @Copyright ©
 */
public class CommonUtils {

    public static<T> List<List<T>> batchList(List<T> sourceList, int batchCount) {
        List<List<T>> returnList = new ArrayList<>();
        int startIndex = 0; // 从第0个下标开始
        while (startIndex < sourceList.size()) {
            int endIndex = 0;
            if (sourceList.size() - batchCount < startIndex) {
                endIndex = sourceList.size();
            } else {
                endIndex = startIndex + batchCount;
            }
            returnList.add(sourceList.subList(startIndex, endIndex));
            startIndex = startIndex + batchCount; // 下一批
        }
        return returnList;
    }
}
