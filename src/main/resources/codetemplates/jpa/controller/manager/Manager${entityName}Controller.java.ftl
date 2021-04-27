package ${package.Controller}.manager;


import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import org.springframework.web.bind.annotation.RequestMapping;
import com.jeecms.cmsx2common.kit.JcResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
<#if swagger2>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;
</#if>
<#if lombok>
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<#else>
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
</#if>
import org.springframework.web.bind.annotation.*;


import java.util.List;


/**
 * <p>
 * ${table.comment!} 前端控制器
 * </p>
 *
 * @author ${author}
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
<#if swagger2>
@Api(value = "${table.comment!}前端控制器", tags = "${table.comment!}-前台")
</#if>
<#if lombok>
@Slf4j
@RequiredArgsConstructor
</#if>
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@RequestMapping("<#if package.ModuleName??>/manager/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class Manager${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class Manager${table.controllerName} extends ${superControllerClass} {
<#else>
public class Manager${table.controllerName} {
</#if>
    <#if !lombok>
    private static final Logger log = LoggerFactory.getLogger(Manager${table.controllerName}.class);
    </#if>

    <#if lombok>
    private final ${table.serviceName} ${cfg.serviceVar};
    <#else>
    @Autowired
    private ${table.serviceName} ${cfg.serviceVar};
    </#if>

    <#if swagger2>
    @ApiOperation(value = "新增${table.comment!}")
    </#if>
    @PostMapping("")
    public JcResult save(@RequestBody ${entity} ${cfg.entityVar}) {
        ${cfg.serviceVar}.save(${cfg.entityVar});
        return JcResult.ok();
    }

    <#if swagger2>
    @ApiOperation(value = "根据id获取${table.comment!}详情")
    </#if>
    @GetMapping("/{id}")
    public JcResult<${entity}> detail(@PathVariable ${cfg.idType} id) {
        ${entity} ${cfg.entityVar} = ${cfg.serviceVar}.getById(id);
        return JcResult.ok(${cfg.entityVar});
    }


    <#if swagger2>
    @ApiOperation(value = "获取${table.comment!}列表")
    </#if>
    @GetMapping("")
    public JcResult<List<${entity}>> list() {
        List<${entity}> list = ${cfg.serviceVar}.list();
        return JcResult.ok(list);
    }

    <#if swagger2>
    @ApiOperation(value = "获取${table.comment!}分页列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageNo", dataType = "integer", paramType = "query", value = "页码, 从1开始计数"),
        @ApiImplicitParam(name = "pageSize", dataType = "integer", paramType = "query", value = "页面大小"),
        @ApiImplicitParam(name = "pageSort", dataType = "string", paramType = "query", value = "排序字段, 格式: name desc,createTime asc")
    })
    </#if>
    @GetMapping("/pages")
    public JcResult<Page<${entity}>> pages(<#if swagger2>@ApiIgnore </#if>Pageable pageParam) {
        Page<${entity}> page = ${cfg.serviceVar}.page(pageParam);
        return JcResult.ok(page);
    }

    <#if swagger2>
    @ApiOperation(value = "根据id删除${table.comment!}", notes = "根据id删除${table.comment!}, 支持批量删除, 多个id以逗号[,]隔开")
    </#if>
    @DeleteMapping("/{ids}")
    public JcResult remove(@PathVariable List<${cfg.idType}> ids) {
        ${cfg.serviceVar}.removeByIds(ids);
        return JcResult.ok();
    }

    <#if swagger2>
    @ApiOperation(value = "修改${table.comment!}")
    </#if>
    @PutMapping("")
    public JcResult modify(@RequestBody ${entity} ${cfg.entityVar}) {
        ${cfg.serviceVar}.save(${cfg.entityVar});
        return JcResult.ok();
    }

}
</#if>