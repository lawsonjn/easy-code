package com.developcollect.easycode.codegen;

import cn.hutool.setting.Setting;
import cn.hutool.setting.SettingUtil;
import com.developcollect.core.utils.FileUtil;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码模板
 *
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/22 13:17
 */
@Data
public class Template {

    public static final String TEMPLATE_SETTING_FILE = "template.setting";

    /**
     * 模板所在文件夹
     */
    private File dir;
    private List<File> templateFiles;

    /**
     * 模板中的空目录
     */
    private List<File> emptyDirs;


    private String logicDelEntity;
    private String physicalDelEntity;
    private String baseController;
    private String baseService;
    private String baseServiceImpl;
    private String baseMapperClass;
    private String baseMapperImplClass;
    private String[] excludeColumns;

    public Template(File dir) {
        this.dir = dir;
        analyze();
    }

    public String getName() {
        return dir.getName();
    }

    private void analyze() {
        Setting setting = SettingUtil.get(new File(this.dir, TEMPLATE_SETTING_FILE).getAbsolutePath());
        logicDelEntity = setting.get("logicDeleteEntity");
        physicalDelEntity = setting.get("physicalDeleteEntity");
        baseController = setting.get("baseController");
        baseService = setting.get("baseService");
        baseServiceImpl = setting.get("baseServiceImpl");
        baseMapperClass = setting.get("baseMapperClass");
        baseMapperImplClass = setting.get("baseMapperImplClass");
        excludeColumns = setting.get("supperColumns").split(", *");


        templateFiles = new ArrayList<>();
        emptyDirs = new ArrayList<>();
        List<File> files = FileUtil.loopDirsAndFiles(this.dir);
        for (File file : files) {
            if (file.isDirectory() && FileUtil.isDirEmpty(file)) {
                emptyDirs.add(file);
            } else if (file.isFile() && file.getName().toLowerCase().endsWith(".ftl")) {
                templateFiles.add(file);
            }
        }
    }
}
