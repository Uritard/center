package ${package.Mapper};

import java.util.List;
import java.util.Date;
import ${package.Entity}.${entity};
import ${superMapperClassPackage};
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author ${author}
 * @since ${date}
 */
<#if kotlin>
interface ${table.mapperName} : ${superMapperClass}<${entity}>
<#else>
@Repository
public interface ${table.mapperName} {

    int insert(${entity} ${table.entityPath});
    int deleteByPrimaryId(<#list table.fields as field><#if field.keyFlag>@Param(value = "${field.propertyName}") ${field.propertyType} ${field.propertyName}</#if></#list>);
    int update(${entity} ${table.entityPath});
    ${entity} selectByPrimaryId(<#list table.fields as field><#if field.keyFlag>@Param(value = "${field.propertyName}") ${field.propertyType} ${field.propertyName}</#if></#list>);
    List<${entity}> select(<#list table.fields as field>@Param(value = "${field.propertyName}") ${field.propertyType} ${field.propertyName}<#if field_has_next>,
                                </#if></#list>);
    List<${entity}> selectByPage(${entity} ${table.entityPath});

    int batchInsert(List<${entity}> list);
}
</#if>
