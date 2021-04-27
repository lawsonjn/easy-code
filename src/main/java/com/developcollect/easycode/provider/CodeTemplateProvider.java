package com.developcollect.easycode.provider;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.codegen.Template;
import com.developcollect.easycode.core.db.DbInfo;
import com.developcollect.easycode.ui.SyncConfCheckBox;
import com.developcollect.easycode.ui.SyncConfComboBox;
import com.developcollect.easycode.ui.SyncConfTextField;
import com.developcollect.easycode.utils.EasyCodeUtil;
import com.developcollect.easycode.utils.UIUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 13:50
 */
public class CodeTemplateProvider implements Provider {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CodeTemplateProvider.class);


    @Override
    public int order() {
        return 1000;
    }

    @Override
    public String getTitle() {
        return "代码生成";
    }

    @Override
    public Component getComponent(JFrame frame) {
        // 释放代码模板
        releaseCodeTemplates();

        JLabel dbLabel = new JLabel("数据库：", SwingConstants.RIGHT);
        JComboBox<DbInfo> dbComboBox = new JComboBox<>();
        List<DbInfo> dbInfos = EASY_CODE_CONFIG.getDbInfos();
        if (!dbInfos.isEmpty()) {
            for (DbInfo dbInfo : EASY_CODE_CONFIG.getDbInfos()) {
                dbComboBox.addItem(dbInfo);
            }
            Integer idx = EASY_CODE_CONFIG.getGenCodeDb();
            idx = idx < dbInfos.size() ? idx : 0;
            dbComboBox.setSelectedIndex(idx);
        }




        JButton dbRefresh = UIUtil.newRefreshButton();
        dbRefresh.setToolTipText("刷新数据库表");

        JLabel dbProjectDir = new JLabel("主项目：", SwingConstants.RIGHT);
        SyncConfTextField dbProjectDirIn = new SyncConfTextField(EASY_CODE_CONFIG::getGcProjectRootDir);



        JLabel dbSubProjectDir = new JLabel("子项目：", SwingConstants.RIGHT);
        JComboBox dbSubProjectDirIn = new SyncConfComboBox(EASY_CODE_CONFIG::getGcSubProject);


        JLabel dbModuleDir = new JLabel("功能模块：", SwingConstants.RIGHT);
        JTextField dbModuleDirIn = new SyncConfTextField(EASY_CODE_CONFIG::getGcModuleName);

        JLabel template = new JLabel("模板：", SwingConstants.RIGHT);
        java.util.List<Template> templates = EasyCodeUtil.scanTemplates();
        List<SyncConfComboBox.Item> items = templates.stream().map(t -> new SyncConfComboBox.Item() {
            @Override
            public String getName() {
                return t.getName();
            }

            @Override
            public Template getData() {
                return t;
            }
        }).collect(Collectors.toList());
        SyncConfComboBox templateIn = new SyncConfComboBox(items, EASY_CODE_CONFIG::getTemplateName);

        JLabel dbPackage = new JLabel("包名：", SwingConstants.RIGHT);
        JTextField dbPackageIn = new SyncConfTextField(EASY_CODE_CONFIG::getGcPackage);

        JLabel dbAuthor = new JLabel("作者：", SwingConstants.RIGHT);
        JTextField dbAuthorIn = new SyncConfTextField(EASY_CODE_CONFIG::getGcAuthor);

        JLabel dbTablePrefix = new JLabel("表前缀：", SwingConstants.RIGHT);
        JTextField dbTablePrefixIn = new SyncConfTextField(EASY_CODE_CONFIG::getGcTablePrefix);

        JLabel dbIdType = new JLabel("　id类型：", SwingConstants.RIGHT);
        SyncConfComboBox dbIdTypeIn = new SyncConfComboBox(new String[]{"Integer", "Long", "String"}, EASY_CODE_CONFIG::getGcIdType);

        JCheckBox dbSwagger2In = new SyncConfCheckBox("swgger2", EASY_CODE_CONFIG::getGcSwagger2);
        JCheckBox dbLombokIn = new SyncConfCheckBox("lombok", EASY_CODE_CONFIG::getGcLombok);

        JCheckBox dbFileOverrideIn = new SyncConfCheckBox("输出覆盖", EASY_CODE_CONFIG::getGcFileOverride);

        JLabel dbTable = new JLabel("表：", SwingConstants.RIGHT);
        SyncConfComboBox dbTableIn = new SyncConfComboBox(EASY_CODE_CONFIG::getGcTable);
        dbTableIn.addItem(EASY_CODE_CONFIG.getGcTable());

        JCheckBox dbLogicDelIn = new SyncConfCheckBox("逻辑删除", EASY_CODE_CONFIG::getGcLogicDel);
        dbLogicDelIn.setSelected(EASY_CODE_CONFIG.getGcLogicDel());

        JButton genBtn = new JButton("生成代码");
        genBtn.setFont(new Font("微软雅黑", Font.BOLD, 22));
        genBtn.setPreferredSize(new Dimension(20, 70));
        genBtn.setMinimumSize(genBtn.getPreferredSize());
        genBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DbInfo dbInfo = (DbInfo) dbComboBox.getSelectedItem();
                if (dbInfo == null) {
                    UIUtil.showWarn("先选择数据库");
                    return;
                }
                EasyCodeUtil.genProjectCode(
                        templateIn.getItem().getData(),
                        dbInfo.getUrl(), dbInfo.getUsername(), dbInfo.getPassword(),
                        dbProjectDirIn.getText(), dbSubProjectDirIn.getSelectedItem().toString(),
                        dbModuleDirIn.getText(), dbPackageIn.getText(),
                        convertIdType(dbIdTypeIn.getSelectedItem().toString()), dbLogicDelIn.isSelected(),
                        dbTableIn.getSelectedItem().toString(), dbTablePrefixIn.getText(), dbAuthorIn.getText(),
                        dbFileOverrideIn.isSelected(), dbSwagger2In.isSelected(), dbLombokIn.isSelected()
                );
                UIUtil.showTip("代码生成完成");
            }
        });


        // 主项目地址，子项目，功能模块，包名，作者，表前缀，id类型，是否逻辑删除
        addRefreshTableBtnListener(dbRefresh, dbComboBox, dbTableIn);
        addRefreshTableComboBoxListener(dbComboBox, dbTableIn);
        addAutoSelectLogicDelListener(dbTableIn, dbLogicDelIn);
        addSubProjectAnalysisListener(dbProjectDirIn, dbSubProjectDirIn);





        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBorder(BorderFactory.createTitledBorder("代码生成"));

        UIUtil.bag(projectPanel, dbLabel, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbComboBox, 0, 1, 0, 0);


        UIUtil.bag(projectPanel, dbProjectDir, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbProjectDirIn, 0, 1, 1, 0);

        UIUtil.bag(projectPanel, dbSubProjectDir, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbSubProjectDirIn, 2, 1, 0, 0);

        UIUtil.bag(projectPanel, template, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, templateIn, 0, 1, 0, 0);

        UIUtil.bag(projectPanel, dbPackage, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbPackageIn, 0, 1, 0, 0);

        UIUtil.bag(projectPanel, dbModuleDir, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbModuleDirIn, 2, 1, 0, 0);
        UIUtil.bag(projectPanel, dbAuthor, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbAuthorIn, 0, 1, 0, 0);

        UIUtil.bag(projectPanel, dbTablePrefix, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbTablePrefixIn, 1, 1, 1, 0);
        UIUtil.bag(projectPanel, dbIdType, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbIdTypeIn, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbFileOverrideIn, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbSwagger2In, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, dbLombokIn, 0, 1, 0, 0);

        UIUtil.bag(projectPanel, dbTable, 1, 1, 0, 0);
        UIUtil.bag(projectPanel, UIUtil.hb(dbTableIn, dbRefresh), 5, 1, 1, 0);
        UIUtil.bag(projectPanel, dbLogicDelIn, 0, 1, 0, 0);


        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));

        Insets insets = new Insets(5, 5, 5, 5);
        UIUtil.bag(panel, projectPanel, 0, 1, 1, 0, insets);
        UIUtil.bag(panel, Box.createVerticalStrut(20), 0, 1, 1, 1);
        UIUtil.bag(panel, genBtn, 0, 1, 1, 0, insets);

        return panel;
    }



    private void addRefreshTableBtnListener(JButton dbRefresh, JComboBox<DbInfo> dbComBoBox, final JComboBox tableComboBox) {
        dbRefresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DbInfo dbInfo = (DbInfo) dbComBoBox.getSelectedItem();
                if (dbInfo == null) {
                    UIUtil.showWarn("先选择数据库");
                    return;
                }
                Object oldSelectedItem = tableComboBox.getSelectedItem();
                List<TableInfo> tableInfos = EasyCodeUtil.getDbTable(dbInfo.getUrl(), dbInfo.getUsername(), dbInfo.getPassword());
                tableComboBox.removeAllItems();
                for (TableInfo tableInfo : tableInfos) {
                    tableComboBox.addItem(new SyncConfComboBox.Item() {
                        @Override
                        public String getName() {
                            return tableInfo.getName();
                        }

                        @Override
                        public <T> T getData() {
                            return (T) tableInfo;
                        }
                    });
                    if (tableInfo.getName().equals(oldSelectedItem)) {
                        tableComboBox.setSelectedItem(oldSelectedItem);
                    }
                }
            }
        });
    }

    private void addRefreshTableComboBoxListener(JComboBox<DbInfo> dbComboBox, final JComboBox tableComboBox) {
        ItemListener itemListener = new ItemListener() {
            private volatile String prevUrl = null;
            private volatile String prevUsername = null;
            private volatile String prevPassword = null;

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e == null || e.getStateChange() == ItemEvent.SELECTED) {
                    if (e != null) {
                        EASY_CODE_CONFIG.setGenCodeDb(dbComboBox.getSelectedIndex());
                    }
                    DbInfo item = (DbInfo) dbComboBox.getSelectedItem();
                    if (item == null) {
                        if (e != null) {
                            UIUtil.showWarn("先选择数据库");
                        }
                        return;
                    }

                    ThreadUtil.execute(() -> {

                        try {
                            String url = item.getUrl();
                            String u = item.getUsername();
                            String p = item.getPassword();

                            boolean dbInfoNoChange = url.equals(prevUrl)
                                    && u.equals(prevUsername)
                                    && p.equals(prevPassword);

                            if (StringUtils.isAnyBlank(url, u, p) || (e != null && dbInfoNoChange)) {
                                return;
                            }
                            prevUrl = url;
                            prevUsername = u;
                            prevPassword = p;

                            Object oldSelectedItem = tableComboBox.getSelectedItem();
                            List<TableInfo> tableInfos = EasyCodeUtil.getDbTable(url, u, p);
                            tableComboBox.removeAllItems();
                            for (TableInfo tableInfo : tableInfos) {
                                tableComboBox.addItem(new SyncConfComboBox.Item() {
                                    @Override
                                    public String getName() {
                                        return tableInfo.getName();
                                    }

                                    @Override
                                    public <T> T getData() {
                                        return (T) tableInfo;
                                    }
                                });
                                if (tableInfo.getName().equals(oldSelectedItem)) {
                                    tableComboBox.setSelectedItem(oldSelectedItem);
                                }
                            }
                        } catch (Exception e1) {
                            log.info("获取表信息失败", e1);
                        }


                    });
                }
            }
        };

        dbComboBox.addItemListener(itemListener);

        // 主动调用一次，用于刷新表
        itemListener.itemStateChanged(null);
    }



    private void addAutoSelectLogicDelListener(SyncConfComboBox tableComboBox, JCheckBox logicDelIn) {
        tableComboBox.addItemListener(new ItemListener() {
            Set<String> deleteFlagSet = new HashSet<>();

            {
                deleteFlagSet.add("delete_flag");
                deleteFlagSet.add("delete_flg");
                deleteFlagSet.add("deleted_flag");
                deleteFlagSet.add("deleted_flg");
                deleteFlagSet.add("deleted");
                deleteFlagSet.add("delete");
            }

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    TableInfo tableInfo = tableComboBox.getItem().getData();
                    for (TableField field : tableInfo.getFields()) {
                        if (deleteFlagSet.contains(field.getName().toLowerCase())) {
                            logicDelIn.setSelected(true);
                            return;
                        }
                    }
                    logicDelIn.setSelected(false);
                }
            }
        });
    }


    private void addSubProjectAnalysisListener(final SyncConfTextField projectDirIn, JComboBox subProjectIn) {
        Consumer<String> consumer = s -> {
            List<String> strings = EasyCodeUtil.analysisSubProject(s);
            Object oldSelectedItem = subProjectIn.getSelectedItem();

            subProjectIn.removeAllItems();
            int start = s.length() + 1;
            for (String string : strings) {
                String item = string.substring(start);
                subProjectIn.addItem(item);
                if (item.equals(oldSelectedItem)) {
                    subProjectIn.setSelectedItem(item);
                }
            }
        };

        UIUtil.setFileChooserField(projectDirIn, "请选择主项目...", JFileChooser.DIRECTORIES_ONLY,
                f -> consumer.accept(f.getAbsolutePath()),
                f -> {
                    if (!FileUtil.exist(f.getAbsolutePath() + File.separator + "pom.xml")) {
                        UIUtil.showInfo(f.getAbsolutePath() + "不是一个合法的Maven项目");
                        return false;
                    }
                    return true;
                });

        projectDirIn.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                consumer.accept(projectDirIn.getText());
            }
        });
    }

    private Class convertIdType(String s) {
        switch (s) {
            case "Long":
                return Long.class;
            case "Integer":
                return Integer.class;
            case "String":
                return String.class;
        }
        return null;
    }

    private void releaseCodeTemplates() {
        if (FileUtil.exist(EASY_CODE_CONFIG.getCodeTemplateHome())) {
            return;
        }

        try {
            EasyCodeUtil.releaseCodeTemplates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
