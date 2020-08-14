package ${package.Service};

import ${package.Entity}.${entity};
import ${superServiceClassPackage};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import java.util.List;
import java.util.Date;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.logs.commons.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author ${author}
* @since ${date}
*/
@Service
<#if kotlin>
open class ${table.serviceName} : ${superServiceClass}<${table.mapperName}, ${entity}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceName}{

    @Autowired
    private ${table.mapperName} ${table.entityPath}Dao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(${entity} ${table.entityPath}) {
        return this.${table.entityPath}Dao.insert(${table.entityPath});
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyType} ${field.propertyName}</#if></#list>) {
        return this.${table.entityPath}Dao.deleteByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyName}</#if></#list>);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(${entity} ${table.entityPath}) {
        return this.${table.entityPath}Dao.update(${table.entityPath});
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public ${entity} selectByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyType} ${field.propertyName}</#if></#list>) {
        return this.${table.entityPath}Dao.selectByPrimaryId(<#list table.fields as field><#if field.keyFlag>${field.propertyName}</#if></#list>);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<${entity}> select(<#list table.fields as field>${field.propertyType} ${field.propertyName}<#if field_has_next>, </#if></#list>) {
        List<${entity}> ${table.entityPath}List = ${table.entityPath}Dao.select(<#list table.fields as field>${field.propertyName}<#if field_has_next>, </#if></#list>);
        return ${table.entityPath}List;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<${entity}> selectByPage(${entity} ${table.entityPath}) {
        List<${entity}> ${table.entityPath}List = ${table.entityPath}Dao.selectByPage(${table.entityPath});
        return ${table.entityPath}List;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<${entity}> list) {
        return this.${table.entityPath}Dao.batchInsert(list);
    }

}
</#if>

<#--/**-->
 <#--* <p>-->
 <#--* ${table.comment!} 服务类-->
 <#--* </p>-->
 <#--*-->
 <#--* @author ${author}-->
 <#--* @since ${date}-->
 <#--*/-->
<#--<#if kotlin>-->
<#--interface ${table.serviceName} : ${superServiceClass}<${entity}>-->
<#--<#else>-->
<#--public interface ${table.serviceName} extends ${superServiceClass}<${entity}> {-->

<#--}-->
<#--</#if>-->
