package com.yjh.platform.common.utils;

import com.yjh.platform.module.device.entity.AreaInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/9/14
 * @since [产品/模块版本] （可选）
 */
public class TreesUtil {

    public static List<AreaInfo> assembleTrees(Collection<AreaInfo> trees) {
        if (CollectionUtils.isEmpty(trees)) {
            return Collections.emptyList();
        }

        // 构建树主键/实例映射表，并初始化树的子节点集合
        Map<?, AreaInfo> mapping = trees.stream().peek(tree -> tree.setChildren(new LinkedList<>()))
                .collect(Collectors.toMap(AreaInfo::getId, t -> t, (o, n) -> n));

        // 查找并关联树节点，返回所有没有父节点的树
        return trees.stream().filter(tree -> {
            AreaInfo parent = ifNull(tree.getUpId(), mapping::get);
            if (parent != null) {
                parent.getChildren().add(tree);
            }
            return Objects.isNull(parent);
        }).collect(Collectors.toList());
    }


    /**
     * 返回不为空的对象（如果第一个对象为空，则返回第二个对象）
     *
     * @param object   目标对象
     * @param function 目标对象方法
     * @param <T>      目标对象类型泛型
     * @param <R>      返回对象类型泛型
     * @return 返回对象
     */
    public static <T, R> R ifNull(T object, Function<T, R> function) {
        return object == null || function == null ? null : function.apply(object);
    }
}
