package com.yjh.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ResultBean
 * @Description:
 * @author: yanhao
 * @date: 2022/9/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultBean {

    private int code;

    private String result;

}


