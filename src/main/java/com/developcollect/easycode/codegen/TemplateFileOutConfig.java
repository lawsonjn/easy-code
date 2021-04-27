package com.developcollect.easycode.codegen;

import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.developcollect.core.utils.BeanUtil;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.core.utils.TextTemplate;

import java.io.File;
import java.util.Iterator;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/23 16:59
 */
public class TemplateFileOutConfig extends FileOutConfig {

    private Template template;
    private File templateFile;

    private PackageConfig packageConfig;
    private String projectPath;
    private String subProjectPath;

    public TemplateFileOutConfig(Template template, File templateFile, PackageConfig packageConfig, String projectPath, String subProjectPath) {
        super(FileUtil.relaPath(template.getDir(), templateFile).replaceAll("\\\\", "/"));
        this.template = template;
        this.templateFile = templateFile;
        this.packageConfig = packageConfig;
        this.projectPath = projectPath;
        this.subProjectPath = subProjectPath;
    }


    @Override
    public String outputFile(TableInfo tableInfo) {
        String s = getTemplatePath();
        Iterator<String> iterator = tableInfo.getImportPackages().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (next.startsWith("com.baomidou")) {
                iterator.remove();
            }
        }
        String packagePath = packageConfig.getParent().replaceAll("\\.", "/");
        String substring = s.substring(0, s.lastIndexOf("/"));
        String name = TextTemplate.mold(templateFile.getName(), BeanUtil.beanToStrMap(tableInfo));
        name = name.substring(0, name.length() - 4);

        String p = projectPath + "/" + subProjectPath + "/src/main/java/" + packagePath + "/" + "/"
                + substring + "/" + name;

        return p;
    }
}
