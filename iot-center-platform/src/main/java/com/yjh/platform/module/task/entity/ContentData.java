package com.yjh.platform.module.task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author YC
 * @date 2020/10/29 - 15:13
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentData {
    int rowCount;
    int columnCount;
    List<TableCellElement> elements;
}
