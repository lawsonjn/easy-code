package com.developcollect.easycode.provider;

import com.developcollect.core.utils.FileUtil;
import com.developcollect.easycode.ProjectEntityListener;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.ui.SwitchButton;
import com.developcollect.easycode.ui.SyncConfTextField;
import com.developcollect.easycode.utils.EasyCodeUtil;
import com.developcollect.easycode.utils.UIUtil;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 13:25
 */
public class QueryDSLProvider implements Provider {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(QueryDSLProvider.class);

    @Override
    public int order() {
        return 2000;
    }

    @Override
    public String getTitle() {
        return "QueryDSL";
    }

    @Override
    public Component getComponent(JFrame parent) {
        JLabel qdProjectDir = new JLabel("项目：", SwingConstants.RIGHT);
        SyncConfTextField qdProjectDirIn = new SyncConfTextField(EASY_CODE_CONFIG::getQdProjectRootDir);
        UIUtil.setFileChooserField(qdProjectDirIn, "选择项目路径", JFileChooser.DIRECTORIES_ONLY);

        JLabel qdEntityPackage = new JLabel("实体路径：", SwingConstants.RIGHT);
        JTextField qdEntityPackageIn = new SyncConfTextField(EASY_CODE_CONFIG::getQdEntityPackage);

        JLabel qdQueryDslCodePath = new JLabel("Q对象路径：", SwingConstants.RIGHT);
        JTextField qdQueryDslCodePathIn = new SyncConfTextField(EASY_CODE_CONFIG::getQdQueryDslCodePath);

        JButton refreshProjectClassPathBtn = new JButton("刷新ClassPath");
        JButton genQueryDslCodeBtn = new JButton("生成Q对象");
        JLabel qdMonitorEntity = new JLabel("监听实体：", SwingConstants.RIGHT);
        JToggleButton qdMonitorEntityTg = new SwitchButton();

        ImageIcon imageIcon = UIUtil.getImageIcon("img/yue.gif");
        JLabel gif = new JLabel();
        gif.setIcon(imageIcon);


        addMonitorEntityToggle(qdProjectDirIn, qdEntityPackageIn, qdQueryDslCodePathIn, qdMonitorEntityTg);
        addGenQueryDslCodeListener(qdProjectDirIn, qdEntityPackageIn, qdQueryDslCodePathIn, genQueryDslCodeBtn);
        addRefreshClassPathListener(qdProjectDirIn, refreshProjectClassPathBtn, parent);



        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBorder(BorderFactory.createTitledBorder("项目设置"));
        UIUtil.bag(projectPanel, qdProjectDir, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, qdProjectDirIn, 0, 1, 1, 0);

        UIUtil.bag(projectPanel, qdEntityPackage, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, qdEntityPackageIn, 4, 1, 1, 0);
        UIUtil.bag(projectPanel, qdQueryDslCodePath, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, qdQueryDslCodePathIn, 0, 1, 0, 0);

        UIUtil.bag(projectPanel, Box.createHorizontalStrut(300), 3, 1, 1, 0);
        UIUtil.bag(projectPanel, qdMonitorEntity, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, qdMonitorEntityTg, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, genQueryDslCodeBtn, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, refreshProjectClassPathBtn, 0, 1, 0, 0);


        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));

        Insets insets = new Insets(5, 5, 5, 5);
        UIUtil.bag(panel, projectPanel, 0, 1, 1, 0, insets);
        UIUtil.bag(panel, Box.createVerticalGlue(), 0, 1, 1, 1, insets);

        return panel;
    }



    private void addMonitorEntityToggle(JTextField pathIn, JTextField entityPathPatternIn, JTextField queryDslCodePath, JToggleButton tg) {
//        tg.addS
        // 添加 toggleBtn 的状态被改变的监听
        tg.addItemListener(new ItemListener() {

            private FileAlterationMonitor monitor;

            private FileAlterationMonitor test(String rootDir) {
                // 监控目录

                // 轮询间隔 1 秒
                long interval = TimeUnit.SECONDS.toMillis(1);

                // 创建过滤器
                IOFileFilter directories = FileFilterUtils.and(
                        FileFilterUtils.directoryFileFilter(),
                        HiddenFileFilter.VISIBLE);


                IOFileFilter entityFilter = null;
                String entityPathPatternInText = entityPathPatternIn.getText();
                if (StringUtils.isNotBlank(entityPathPatternInText)) {
                    java.util.List<String> patterns;
                    if (entityPathPatternInText.contains("|")) {
                        patterns = Arrays.asList(entityPathPatternInText.split("\\|"));
                    } else {
                        patterns = Collections.singletonList(entityPathPatternInText);
                    }

                    // include
                    List<PathMatcher> includeMatchers = patterns.stream()
                            .map(s -> s + "/**.java")
                            .map(FileUtil::dressPathPattern)
                            .map(p -> FileSystems.getDefault().getPathMatcher(p))
                            .collect(Collectors.toList());


                    entityFilter = new AbstractFileFilter() {
                        @Override
                        public boolean accept(File file) {
                            for (PathMatcher matcher : includeMatchers) {
                                if (matcher.matches(Paths.get(file.toURI()))) {
                                    // todo  优化q对象的过滤条件，Q对象本身变化不触发自动生成Q对象
                                    if (file.getName().startsWith("Q")) {
                                        return false;
                                    }
                                    return true;
                                }
                            }
                            return false;
                        }
                    };
                }


                IOFileFilter files = FileFilterUtils.and(
                        FileFilterUtils.fileFileFilter(),
                        entityFilter == null
                                ? FileFilterUtils.suffixFileFilter(".java")
                                : entityFilter
                );

                IOFileFilter filter = FileFilterUtils.or(directories, files);
                // 使用过滤器
                FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir), filter);
                //不使用过滤器
                //FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));

                //创建文件变化监听器
                FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
                return monitor;
            }

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED && e.getStateChange() != ItemEvent.DESELECTED) {
                    return;
                }

                JToggleButton toggleButton = (JToggleButton) e.getSource();
                if (toggleButton.isSelected()) {
                    // 1. 监控文件变化
                    // 2. 变化了就生成Q对象代码
                    if (monitor != null) {
                        try {
                            monitor.stop();
                        } catch (Exception e1) {
                        }
                    }
                    monitor = test(pathIn.getText());
                    monitor.getObservers().forEach(o -> o.addListener(new ProjectEntityListener(pathIn.getText(), queryDslCodePath.getText())));

                    // 开始监控
                    try {
                        monitor.start();
                        log.info("开始监控[{}]", pathIn.getText());
                        UIUtil.showTip("开始监控[{}]", pathIn.getText());
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                } else {
                    try {
                        if (monitor != null) {
                            monitor.stop();
                            log.info("停止监控[{}]", pathIn.getText());
                            UIUtil.showTip("停止监控[{}]", pathIn.getText());
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });

    }


    private void addGenQueryDslCodeListener(JTextField pathIn, JTextField entityPathPatternIn, JTextField queryDslCodePath, JButton btn) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String projectPath = pathIn.getText();
                    //      根据entity路径生成q对象路径
                    String outDir = EasyCodeUtil.getQueryDslCodeOutPath(projectPath);

                    // 获取项目编译cp
                    String qdCps = EASY_CODE_CONFIG.getQdCps();
                    if (StringUtils.isBlank(qdCps)) {
                        List<String> compilePaths = EasyCodeUtil.getCompilePaths(projectPath);
                        // 加入本地缓存cp
                        compilePaths.add(outDir);
                        qdCps = StringUtils.join(compilePaths, ";");
                        EASY_CODE_CONFIG.setQdCps(qdCps);
                    }

                    // 提取出所有entity路径
                    List<File> entityDirs = FileUtil.loopDirsAndFilesByPattern(projectPath, entityPathPatternIn.getText());

                    // 调用, 生成q对象
                    boolean ret = EasyCodeUtil.genQueryDslCode(new HashSet<>(entityDirs), qdCps, outDir);

                    //  替换, 保存新的Q对象
                    EasyCodeUtil.syncQueryDslFile(projectPath, outDir, queryDslCodePath.getText());

                    /*
                     * 取出所有java文件
                     * 替换包名
                     * 替换引用包名
                     */
                    if (ret) {
                        UIUtil.showTip("生成成功");
                    } else {
                        UIUtil.showWarn("生成失败");
                    }
                } catch (Exception e1) {
                    UIUtil.showError("生成失败：" + e1.getMessage());
                }
            }
        });
    }


    private void addRefreshClassPathListener(JTextField pathIn, JButton refreshProjectClassPathBtn, JFrame frame) {
        refreshProjectClassPathBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 获取项目编译cp
                // 在执行项目执行mvn compile, 然后把编译出来的classes复制到工作区，以供后续使用
                try {
                    String projectPath = pathIn.getText();
                    File outDir = new File(EasyCodeUtil.getQueryDslCodeClassesPath(projectPath));
                    // 删除之前的
                    if (FileUtil.exist(outDir)) {
                        FileUtil.del(outDir);
                        FileUtil.del(new File(EasyCodeUtil.getQueryDslCodeOutPath(projectPath)));
                    }
                    List<String> compilePaths = EasyCodeUtil.getCompilePaths(projectPath);
                    // 获取项目中的classes
                    List<File> dirs = FileUtil.loopDirsByPattern(projectPath, "**/**/target/classes");
                    for (File dir : dirs) {
                        FileUtil.copy(dir, outDir, true);
                    }

                    compilePaths.add(outDir.getAbsolutePath() + "/classes");
                    String qdCps = StringUtils.join(compilePaths, ";");
                    EASY_CODE_CONFIG.setQdCps(qdCps);
                    JOptionPane.showMessageDialog(frame, "编译成功", "编译成功", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, "编译失败", "编译失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

}
