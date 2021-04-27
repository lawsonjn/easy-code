package ${thisPkg};

import ${.globals["/entity/jpa/$\{entityName}.java.ftl"]};
import ${superMapperImplClassPackage};
<#if lombok>
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<#else>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
</#if>


/**
 * <p>
 * ${table.comment!} Repository 接口
 * </p>
 *
 * @author ${author}
 * @version 1.0
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
<#if lombok>
@Slf4j
</#if>
public class ${entity}RepositoryImpl extends ${superMapperImplClass}<${entity}, ${cfg.idType}> {

    <#if !lombok>
    private static final Logger log = LoggerFactory.getLogger(${entity}RepositoryImpl.class);
    </#if>

}

