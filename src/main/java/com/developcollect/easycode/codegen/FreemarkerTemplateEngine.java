package com.developcollect.easycode.codegen;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.ConstVal;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.engine.AbstractTemplateEngine;
import com.developcollect.core.utils.BeanUtil;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.core.utils.TextTemplate;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/22 16:08
 */
public class FreemarkerTemplateEngine extends AbstractTemplateEngine {

    private Configuration configuration;
    private File dir;

    public FreemarkerTemplateEngine(File dir) {
        this.dir = dir;
    }

    @Override
    public FreemarkerTemplateEngine init(ConfigBuilder configBuilder) {
        super.init(configBuilder);
        configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        try {
            configuration.setDirectoryForTemplateLoading(dir);
        } catch (IOException e) {
            LambdaUtil.raise(e);
        }
        configuration.setDefaultEncoding(ConstVal.UTF8);
//        configuration.setClassForTemplateLoading(FreemarkerTemplateEngine.class, StringPool.SLASH);
//        configuration.getTe
        return this;
    }

    @Override
    public AbstractTemplateEngine batchOutput() {
        try {
            List<TableInfo> tableInfoList = getConfigBuilder().getTableInfoList();
            for (TableInfo tableInfo : tableInfoList) {
                Map<String, Object> objectMap = getObjectMap(tableInfo);
                // 清除objectMap中不必要的数据
                clearObjectMap(objectMap);

                // 自定义内容
                InjectionConfig injectionConfig = getConfigBuilder().getInjectionConfig();
                if (null != injectionConfig) {
                    injectionConfig.initMap();
                    objectMap.put("cfg", injectionConfig.getMap());
                    List<FileOutConfig> focList = injectionConfig.getFileOutConfigList();
                    if (CollectionUtils.isNotEmpty(focList)) {
                        for (FileOutConfig foc : focList) {
                            if (isCreate(FileType.OTHER, foc.outputFile(tableInfo))) {
                                dressObjectMap(objectMap, foc);
                            }
                        }
                        for (FileOutConfig foc : focList) {
                            if (isCreate(FileType.OTHER, foc.outputFile(tableInfo))) {
                                writer(dressThisObjectMap(objectMap, foc), foc.getTemplatePath(), foc.outputFile(tableInfo));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("无法创建文件，请检查配置信息！", e);
        }
        return this;
    }


    @Override
    public void writer(Map<String, Object> objectMap, String templatePath, String outputFile) throws Exception {
        Template template = configuration.getTemplate(templatePath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            template.process(objectMap, new OutputStreamWriter(fileOutputStream, ConstVal.UTF8));
        }
        logger.debug("模板:" + templatePath + ";  文件:" + outputFile);
    }


    @Override
    public Map<String, Object> getObjectMap(TableInfo tableInfo) {
        Map<String, Object> objectMap = new HashMap<>(30);
        ConfigBuilder config = getConfigBuilder();
        CusStrategyConfig strategyConfig = (CusStrategyConfig) config.getStrategyConfig();
        if (config.getStrategyConfig().isControllerMappingHyphenStyle()) {
            objectMap.put("controllerMappingHyphenStyle", config.getStrategyConfig().isControllerMappingHyphenStyle());
            objectMap.put("controllerMappingHyphen", StringUtils.camelToHyphen(tableInfo.getEntityPath()));
        }
        objectMap.put("restControllerStyle", config.getStrategyConfig().isRestControllerStyle());
        objectMap.put("config", config);
        objectMap.put("package", getPackageInfo(config));
        GlobalConfig globalConfig = config.getGlobalConfig();
        objectMap.put("author", globalConfig.getAuthor());
        objectMap.put("idType", globalConfig.getIdType() == null ? null : globalConfig.getIdType().toString());
        objectMap.put("logicDeleteFieldName", config.getStrategyConfig().getLogicDeleteFieldName());
        objectMap.put("versionFieldName", config.getStrategyConfig().getVersionFieldName());
        objectMap.put("activeRecord", globalConfig.isActiveRecord());
        objectMap.put("kotlin", globalConfig.isKotlin());
        objectMap.put("swagger2", globalConfig.isSwagger2());
        objectMap.put("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        objectMap.put("table", tableInfo);
        objectMap.put("enableCache", globalConfig.isEnableCache());
        objectMap.put("baseResultMap", globalConfig.isBaseResultMap());
        objectMap.put("baseColumnList", globalConfig.isBaseColumnList());
        objectMap.put("entity", tableInfo.getEntityName());
        objectMap.put("entityName", objectMap.get("entity"));
        objectMap.put("entitySerialVersionUID", config.getStrategyConfig().isEntitySerialVersionUID());
        objectMap.put("entityColumnConstant", config.getStrategyConfig().isEntityColumnConstant());
        objectMap.put("entityBuilderModel", config.getStrategyConfig().isEntityBuilderModel());
        objectMap.put("entityLombokModel", config.getStrategyConfig().isEntityLombokModel());
        objectMap.put("lombok", objectMap.get("entityLombokModel"));
        objectMap.put("entityBooleanColumnRemoveIsPrefix", config.getStrategyConfig().isEntityBooleanColumnRemoveIsPrefix());
        objectMap.put("superEntityClassPackage", config.getSuperEntityClass());
        objectMap.put("superEntityClass", getSuperClassName(config.getSuperEntityClass()));
        objectMap.put("superMapperClassPackage", config.getSuperMapperClass());
        objectMap.put("superMapperClass", getSuperClassName(config.getSuperMapperClass()));
        objectMap.put("superMapperImplClassPackage", strategyConfig.getSuperMapperImplClass());
        objectMap.put("superMapperImplClass", getSuperClassName(strategyConfig.getSuperMapperImplClass()));
        objectMap.put("superServiceClassPackage", config.getSuperServiceClass());
        objectMap.put("superServiceClass", getSuperClassName(config.getSuperServiceClass()));
        objectMap.put("superServiceImplClassPackage", config.getSuperServiceImplClass());
        objectMap.put("superServiceImplClass", getSuperClassName(config.getSuperServiceImplClass()));
        objectMap.put("superControllerClassPackage", config.getSuperControllerClass());
        objectMap.put("superControllerClass", getSuperClassName(config.getSuperControllerClass()));

        return Objects.isNull(config.getInjectionConfig()) ? objectMap : config.getInjectionConfig().prepareObjectMap(objectMap);
    }

    private void clearObjectMap(Map<String, Object> objectMap) {
        // 把table里的imports分离
        TableInfo tableInfo = (TableInfo) objectMap.get("table");
        Set<String> importPackages = tableInfo.getImportPackages();
        importPackages.remove(getConfigBuilder().getSuperEntityClass());
    }


    protected Map<String, Object> dressObjectMap(Map<String, Object> objectMap, FileOutConfig foc) {
        String superControllerClassPackage = ((Map<String, String>) objectMap.get("package")).get("Entity");
        String parent = superControllerClassPackage.substring(0, superControllerClassPackage.lastIndexOf("."));
        String templatePath = foc.getTemplatePath();
        String substring = templatePath.substring(1, templatePath.lastIndexOf("/")).replaceAll("/", ".");

        // pkg
        objectMap.put(foc.getTemplatePath() + ".pkg", joinPackage(parent, substring));
        // ref
        String ref = TextTemplate.mold(templatePath, BeanUtil.beanToStrMap(getConfigBuilder().getTableInfoList().get(0))).substring(1);
        // .java.ftl 共9个字符
        ref = ref.substring(0, ref.length() - 9).replaceAll("/", ".");
        objectMap.put(foc.getTemplatePath(), joinPackage(parent, ref));

        return objectMap;
    }

    protected Map<String, Object> dressThisObjectMap(Map<String, Object> objectMap, FileOutConfig foc) {
        objectMap.put("thisPkg", objectMap.get(foc.getTemplatePath() + ".pkg"));
        return objectMap;
    }

    /**
     * 获取类名
     *
     * @param classPath ignore
     * @return ignore
     */
    private String getSuperClassName(String classPath) {
        if (StringUtils.isEmpty(classPath)) {
            return null;
        }
        return classPath.substring(classPath.lastIndexOf(StringPool.DOT) + 1);
    }


    protected Map getPackageInfo(ConfigBuilder config) {
        Map<String, String> packageInfo = config.getPackageInfo();
//        packageInfo.put(ConstVal.ENTITY, joinPackage(config.getParent(), config.getEntity()));
//        int lastDotIdx = packageInfo.get(ConstVal.MAPPER).lastIndexOf(StringPool.DOT);
//        String str1 = packageInfo.get(ConstVal.MAPPER).substring(0, lastDotIdx);
//
//        packageInfo.put(ConstVal.MAPPER, "");
//        packageInfo.put(ConstVal.XML, joinPackage(config.getParent(), config.getXml()));
//        packageInfo.put(ConstVal.SERVICE, joinPackage(config.getParent(), config.getService()));
//        packageInfo.put(ConstVal.SERVICE_IMPL, joinPackage(config.getParent(), config.getServiceImpl()));
//        packageInfo.put(ConstVal.CONTROLLER, joinPackage(config.getParent(), config.getController()));
        // 根据路径拼接包名
        return packageInfo;
    }


    /**
     * 连接父子包名
     *
     * @param parent     父包名
     * @param subPackage 子包名
     * @return 连接后的包名
     */
    private String joinPackage(String parent, String subPackage) {
        if (StringUtils.isEmpty(parent)) {
            return subPackage;
        }
        return parent + StringPool.DOT + subPackage;
    }

    @Override
    public String templateFilePath(String filePath) {
        return filePath + ".ftl";
    }
}

