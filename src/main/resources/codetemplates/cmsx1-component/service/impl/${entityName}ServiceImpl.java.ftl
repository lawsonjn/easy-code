package ${package.ServiceImpl};

import ${package.Entity}.${entity};
import ${.globals["/dao/$\{entityName}Dao.java.ftl"]};
import ${.globals["/service/$\{entityName}Service.java.ftl"]};
import ${superServiceImplClassPackage};
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
<#if lombok>
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<#else>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
</#if>

/**
 * <p>
 * ${table.comment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
<#if lombok>
@Slf4j
@RequiredArgsConstructor
</#if>
@Service
@Transactional(rollbackFor = Exception.class)
<#if kotlin>
open class ${table.serviceImplName} : ${superServiceImplClass}<${table.mapperName}, ${entity}, ${cfg.idType}>(), ${table.serviceName} {

}
<#else>
public class ${table.serviceImplName} extends ${superServiceImplClass}<${entity}, ${entity}Dao, ${cfg.idType}> implements ${entity}Service {

    <#if !lombok>
    private static final Logger log = LoggerFactory.getLogger(${table.serviceImplName}.class);
    </#if>





}
</#if>
