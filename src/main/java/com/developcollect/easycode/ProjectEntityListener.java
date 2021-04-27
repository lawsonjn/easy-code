package com.developcollect.easycode;

import com.developcollect.core.utils.FileUtil;
import com.developcollect.easycode.utils.EasyCodeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * 实体类监听器，在实体类变化后自动生成Q对象
 *
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/14 16:22
 */
@Slf4j
@Data
@AllArgsConstructor
public class ProjectEntityListener extends FileAlterationListenerAdaptor {

    private String projectPath;
    private String offsetPath;


    @Override
    public void onFileCreate(File file) {
        test(file);
    }

    @Override
    public void onFileChange(File file) {
        test(file);
    }

    @Override
    public void onFileDelete(File file) {
        // 获取项目路径 + q对象路径
        String q = file.getParentFile().getAbsolutePath() + File.separator + offsetPath + File.separator + "Q" + file.getName();
        FileUtil.del(q);
    }

    protected void test(File monitorFile) {
        try {
            //      根据entity路径生成q对象路径
            String outDir = EasyCodeUtil.getQueryDslCodeOutPath(projectPath);

            // 获取项目编译cp
            String qdCps = EASY_CODE_CONFIG.getQdCps();
            if (StringUtils.isBlank(qdCps)) {
                try {
                    File outClassesDir = new File(EasyCodeUtil.getQueryDslCodeClassesPath(projectPath));
                    if (FileUtil.exist(outClassesDir)) {
                        FileUtil.mkdir(outClassesDir);
                    }
                    List<String> compilePaths = EasyCodeUtil.getCompilePaths(projectPath);
                    // 获取项目中的classes
                    List<File> dirs = FileUtil.loopDirsByPattern(projectPath, "**/**/target/classes");
                    for (File dir : dirs) {
                        FileUtil.copy(dir, outClassesDir, true);
                    }
//                    FileUtil.copy()
                    // 加入本地缓存cp
                    compilePaths.add(outClassesDir.getAbsolutePath() + "/classes");
                    qdCps = StringUtils.join(compilePaths, ";");
                    EASY_CODE_CONFIG.setQdCps(qdCps);
                } catch (Exception e1) {
                    log.error("qttest", e1);
                }

            }

            // 提取出所有entity路径
            List<File> entityDirs = Arrays.asList(monitorFile);

            // 调用, 生成q对象
            boolean ret = EasyCodeUtil.genQueryDslCode(new HashSet<>(entityDirs), qdCps, outDir);

            //  替换, 保存新的Q对象
            EasyCodeUtil.syncQueryDslFile(projectPath, outDir, offsetPath);

            /*
             * 取出所有java文件
             * 替换包名
             * 替换引用包名
             */
            if (ret) {
                log.info("监控文件[{}], 自动生成Q对象成功");
            } else {
                log.info("监控文件[{}], 自动生成Q对象失败");
            }
        } catch (Exception e1) {
            log.info("监控文件[{}], 自动生成Q对象失败", e1);
        }
    }


}
