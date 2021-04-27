package ${package.Controller}.admin;


import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import org.springframework.web.bind.annotation.RequestMapping;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jeecms.market.kit.JcResult;
<#if restControllerStyle>
import org.springframework.web.bind.annotation.RestController;
<#else>
import org.springframework.stereotype.Controller;
</#if>
<#if superControllerClassPackage??>
import ${superControllerClassPackage};
</#if>
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
@Api(value = "${table.comment!}前端控制器", tags = "${table.comment!}-管理平台")
<#if restControllerStyle>
@RestController
<#else>
@Controller
</#if>
@Slf4j
@RequiredArgsConstructor
@RequestMapping("<#if package.ModuleName??>/admin/${package.ModuleName}</#if>/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>")
<#if kotlin>
class Admin${table.controllerName}<#if superControllerClass??> : ${superControllerClass}()</#if>
<#else>
<#if superControllerClass??>
public class Admin${table.controllerName} extends ${superControllerClass} {
<#else>
public class Admin${table.controllerName} {
</#if>

    private final ${table.serviceName} ${cfg.serviceVar};


    @ApiOperation(value = "新增${table.comment!}")
    @PostMapping("")
    public JcResult save(@RequestBody ${entity} ${cfg.entityVar}) {
        ${cfg.serviceVar}.save(${cfg.entityVar});
        return JcResult.ok();
    }

    @ApiOperation(value = "根据id获取${table.comment!}详情")
    @GetMapping("/{id}")
    public JcResult<${entity}> detail(@PathVariable ${cfg.idType} id) {
        ${entity} ${cfg.entityVar} = ${cfg.serviceVar}.getById(id);
        return JcResult.okData(${cfg.entityVar});
    }


    @ApiOperation(value = "获取${table.comment!}列表")
    @GetMapping("")
    public JcResult<List<${entity}>> list() {
        List<${entity}> list = ${cfg.serviceVar}.list();
        return JcResult.okData(list);
    }

    @ApiOperation(value = "获取${table.comment!}分页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", dataType = "integer", paramType = "query", value = "页码, 从1开始计数"),
            @ApiImplicitParam(name = "pageSize", dataType = "integer", paramType = "query", value = "页面大小"),
            @ApiImplicitParam(name = "pageSort", dataType = "string", paramType = "query", value = "排序字段, 格式: name desc,createTime asc")
    })
    @GetMapping("/pages")
    public JcResult<IPage<${entity}>> pages(@ApiIgnore IPage<${entity}> pageParam) {
        IPage<${entity}> page = ${cfg.serviceVar}.page(pageParam);
        return JcResult.okData(page);
    }

    @ApiOperation(value = "根据id删除${table.comment!}", notes = "根据id删除${table.comment!}, 支持批量删除, 多个id以逗号[,]隔开")
    @DeleteMapping("/{ids}")
    public JcResult remove(@PathVariable List<${cfg.idType}> ids) {
        ${cfg.serviceVar}.removeByIds(ids);
        return JcResult.ok();
    }

    @ApiOperation(value = "修改${table.comment!}")
    @PutMapping("")
    public JcResult modify(@RequestBody ${entity} ${cfg.entityVar}) {
        ${cfg.serviceVar}.updateById(${cfg.entityVar});
        return JcResult.ok();
    }

}
</#if>