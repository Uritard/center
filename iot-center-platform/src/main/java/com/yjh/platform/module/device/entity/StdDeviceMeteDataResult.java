package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 丫C
 * @date 2022/7/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PageResult对象", description = "分页")
public class StdDeviceMeteDataResult {
    @ApiModelProperty(value = "总条数")
    private Integer count;

    @ApiModelProperty(value = "总页数")
    private Integer countPage;

    @ApiModelProperty(value = "当前页")
    private Integer pageNum;

    @ApiModelProperty(value = "当前页显示多少条记录")
    private Integer pageSize;

    @ApiModelProperty(value = "结果集")
    private List<TStdDeviceMeteDetail> list;

    public StdDeviceMeteDataResult(List<TStdDeviceMeteDetail> list, Integer pageNum, Integer pageSize){

        this.pageNum  = pageNum;
        this.pageSize = pageSize;
        this.count = list.size();

        // 总记录数和每页显示的记录之间是否可以凑成整数（pages）
        boolean full = count % pageSize == 0;
        // 分页 == 根据pageSize（每页显示的记录数）计算pages
        if(Boolean.FALSE.equals(full)){
            // 如果凑不成整数
            this.countPage = count / pageSize + 1;
        }else{
            // 如果凑成整数
            this.countPage = count / pageSize;
        }

        int fromIndex = pageNum * pageSize - pageSize;
        int toIndex = 0;

        if(pageNum == 0){
            throw new ArithmeticException("第0页无法展示");
        }else if(pageNum > countPage){
            // 如果查询的页码数大于总的页码数，list设置为[]
            list = new ArrayList<>();
        }else if(Objects.equals(pageNum, countPage)){
            // 如果查询的当前页等于总页数，直接索引到total处
            toIndex = count;
        }else{
            // 如果查询的页码数小于总页数，不用担心切割List的时候toIndex索引会越界，直接等
            toIndex = pageNum * pageSize;
        }

        if(list.isEmpty()){
            this.list = list;
        }else{
            this.list = list.subList(fromIndex, toIndex);
        }

    }
}
