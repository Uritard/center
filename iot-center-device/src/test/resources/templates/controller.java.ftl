package ${package.Controller};

import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import io.swagger.annotations.*;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.device.commons.result.Result;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import java.util.Map;
import com.yjh.device.commons.result.Result;
import com.yjh.device.commons.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.jdbc.StringUtils;

<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>

/**
 * @author ${author}
 * @since ${date}
 */
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>/v1")
@Api(value = "/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>", description = "${table.comment!}操作接口")
<#if kotlin>
class ${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class ${table.controllerName} extends ${superControllerClass} {
<#else>
public class ${table.controllerName} {
</#if>

    @Autowired
    private final ${table.serviceName} ${table.entityPath}Service;

    private Logger log = LoggerFactory.getLogger(${table.controllerName}.class);

    public ${table.controllerName}(${table.serviceName} ${table.entityPath}Service) {
        this.${table.entityPath}Service = ${table.entityPath}Service;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/insert", method = RequestMethod.POST)
    public Result insert(@RequestBody ${entity} ${table.entityPath}) {
        Result result = new Result();
        try {
            result.setData(${table.entityPath}Service.insert(${table.entityPath}));
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public Result delete(<#list table.fields as field><#if field.keyFlag>@RequestParam(value = "${field.propertyName}", required = true) ${field.propertyType} ${field.propertyName}</#if></#list>) {
        Result result = new Result();
        try {
            result.setData(${table.entityPath}Service.deleteByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyName}</#if></#list>));
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新")
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    public Result update(@RequestBody ${entity} ${table.entityPath}) {
        Result result = new Result();
        try {
            result.setData(${table.entityPath}Service.update(${table.entityPath}));
        } catch (Exception e) {
            if (StringUtils.indexOfIgnoreCase(e.getCause().getMessage(), "idx_sys_role_name") != -1) {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), "业务描述");
            } else {
                result.setCode(ResultCodeEnum.CREATEORUPDATEERROR.getCode(), ResultCodeEnum.CREATEORUPDATEERROR.getName());
            }
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    public Result selectByPrimaryId(<#list table.fields as field><#if field.keyFlag>@RequestParam(value = "${field.propertyName}", required = true) ${field.propertyType} ${field.propertyName}</#if></#list>) {
        Result result = new Result();
        try {
            ${entity} ${table.entityPath} = ${table.entityPath}Service.selectByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyName}</#if></#list>);
            result.setData(${table.entityPath});
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public Result select(<#list table.fields as field>@RequestParam(value = "${field.propertyName}", required = false) ${field.propertyType} ${field.propertyName}<#if field_has_next>,
                            </#if></#list>) {
        Result result = new Result();
        try {
            List<${entity}> list = ${table.entityPath}Service.select(<#list table.fields as field>${field.propertyName}<#if field_has_next>, </#if></#list>);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.POST)
    public Result selectByPage(@RequestBody ${entity} ${table.entityPath},
                                @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                @RequestParam(value = "pageSize", required = false, defaultValue = "100") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize);
            List<${entity}> list = ${table.entityPath}Service.selectByPage(${table.entityPath});
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
</#if>
