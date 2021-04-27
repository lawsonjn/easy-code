package ${thisPkg};

<#list table.importPackages as pkg>
import ${pkg};
</#list>
<#if swagger2>
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
</#if>
<#if entityLombokModel>
import lombok.Data;
import lombok.experimental.Accessors;
</#if>
<#if (cfg.idImportName)??>
import ${cfg.idImportName};
</#if>
import ${.globals["/entity/jpa/$\{entityName}.java.ftl"]};


/**
 * <p>
 * ${table.comment!}VO
 * </p>
 *
 * @author ${author}
 * @date ${date}
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
<#if entityLombokModel>
@Data
@Accessors(chain = true)
</#if>
<#if swagger2>
@ApiModel(value="Admin${entity}VO"<#if table.comment! != ''>, description="${table.comment!}VO"</#if>)
</#if>
public class Admin${entity}VO implements Serializable {


    <#if entitySerialVersionUID>
    private static final long serialVersionUID = ${cfg.entityUid?c}L;
    </#if>
	
	<#if swagger2>
    @ApiModelProperty(value = "${table.comment!}id")
	<#else>
    /**
     * ${table.comment!}id
     */
	</#if>
	private Long id;
<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
    <#if field.keyFlag>
        <#assign keyPropertyName="${field.propertyName}"/>
    </#if>

    <#if field.comment!?length gt 0>
        <#if swagger2>
    @ApiModelProperty(value = "${field.comment}")
        <#else>
    /**
     * ${field.comment}
     */
        </#if>
    </#if>
    private ${field.propertyType} ${field.propertyName};
</#list>
<#------------  END 字段循环遍历  ---------->


    public Long getId() {
	    return id;
	}
	
	public void setId(Long id) {
	    this.id = id;
	}
<#if !entityLombokModel>
    <#list table.fields as field>
        <#if field.propertyType == "boolean">
            <#assign getprefix="is"/>
        <#else>
            <#assign getprefix="get"/>
        </#if>

    public ${field.propertyType} ${getprefix}${field.capitalName}() {
        return ${field.propertyName};
    }

        <#if entityBuilderModel>
    public ${entity} set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
        <#else>
    public void set${field.capitalName}(${field.propertyType} ${field.propertyName}) {
        </#if>
        this.${field.propertyName} = ${field.propertyName};
        <#if entityBuilderModel>
        return this;
        </#if>
    }
    </#list>
</#if>

<#if entityColumnConstant>
    <#list table.fields as field>
    public static final String ${field.name?upper_case} = "${field.name}";

    </#list>
</#if>


	public static Admin${entity}VO of(${entity} ${cfg.entityVar}) {
        Admin${entity}VO vo = new Admin${entity}VO();
		vo.setId(${cfg.entityVar}.getId());
	<#list table.fields as field>
		vo.set${field.capitalName}(${cfg.entityVar}.get${field.capitalName}());
	</#list>

        return vo;
    }

<#if !entityLombokModel>
    @Override
    public String toString() {
    return "${entity}{" +
	            "id=" + id +
    <#list table.fields as field>
                ", ${field.propertyName}=" + ${field.propertyName} +
    </#list>
           "}";
    }
</#if>
}
