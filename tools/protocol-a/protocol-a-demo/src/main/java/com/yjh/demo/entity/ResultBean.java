package com.yjh.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @ClassName: ResultBean
 * @Description:
 * @author: yanhao
 * @date: 2022/9/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ResultBean {

    private int code;

    private String result;

    private Object data;

    public ResultBean(int code, String result) {
        this.code = code;
        this.result = result;
    }
}


