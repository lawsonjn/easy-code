package com.developcollect.easycode.provider;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.developcollect.core.task.DelayTask;
import com.developcollect.core.task.TaskUtil;
import com.developcollect.core.thread.ThreadUtil;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.core.utils.JarFileUtil;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.ui.IconTextField;
import com.developcollect.easycode.ui.SDialog;
import com.developcollect.easycode.utils.UIUtil;
import lombok.Data;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * https://raw.githubusercontent.com/any86/any-rule/v0.3.7/packages/www/src/RULES.js
 *
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/26 14:00
 */
public class RegexProvider implements Provider {
    @Override
    public int order() {
        return 5000;
    }

    @Override
    public String getTitle() {
        return "正则";
    }

    @Override
    public Component getComponent(JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
        List<Rule> rules = loadRules();


        JList<Rule> listView = new JList<>(UIUtil.toListModel(rules));
        JScrollPane jsp = new JScrollPane(listView);

        listView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listView.getSelectedIndex() == -1) {
                    return;
                }

                // 双击
                if (e.getClickCount() == 2) {
                    Rule selectedRule = listView.getSelectedValue();
                    // 弹出测试窗口
                    SDialog.builder().title("正则表达式测试").parent(frame).body(regexTestPanel(selectedRule)).build().display();
                }
            }
        });

        // 替换默认的复制操作
        listView.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listView.getSelectedIndex() == -1) {
                    return;
                }
                Rule selectedRule = listView.getSelectedValue();
                UIUtil.setClipboardString(selectedRule.rule);
            }
        });


        IconTextField searchField = new IconTextField();
        UIUtil.setSearchField(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {

            private volatile boolean searched = false;

            @Override
            public void insertUpdate(DocumentEvent e) {
                doSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            private synchronized void doSearch() {
                String text = searchField.getText();
                if (StrUtil.isBlank(text) && searched) {
                    listView.setModel(UIUtil.toListModel(rules));
                    searched = false;
                } else {
                    if (searchField.isHint()) {
                        return;
                    }
                    List<Rule> newList = rules.stream().filter(rule -> rule.title.contains(text)).collect(Collectors.toList());
                    listView.setModel(UIUtil.toListModel(newList));
                    searched = true;
                }
            }
        });

        UIUtil.bag(panel, searchField, 0, 1, 1, 0);
        UIUtil.bag(panel, jsp, 1, 1, 1, 1);

        return panel;
    }


    private List<Rule> loadRules() {
        File rulesFile = new File(EASY_CODE_CONFIG.getHomeDir() + "/misc/RegexRules.json");
        try {
            return JSON.parseArray(FileUtil.readAll(rulesFile), Rule.class);
        } catch (Exception ignore) {
            JarFileUtil.copy("/misc/RegexRules.json", EASY_CODE_CONFIG.getHomeDir() + "/misc/", RegexProvider.class);
            return JSON.parseArray(FileUtil.readAll(rulesFile), Rule.class);
        }
    }





    private JPanel regexTestPanel(Rule rule) {
        JPanel panel = new JPanel(new GridBagLayout());
        // 设置只读
        JTextArea ruleField = UIUtil.onlyShowText(new JTextArea(rule.getRule()));
        JTextField matchResultField = UIUtil.onlyShowText(new JTextField());

        JTextArea textArea = new JTextArea();
        JScrollPane jsp = new JScrollPane(textArea);

        UIUtil.bag(panel, ruleField, 0, 1, 1, 0);
        UIUtil.bag(panel, jsp, 0, 1, 1, 1);
        UIUtil.bag(panel, matchResultField, 0, 1, 1, 0);

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            private DelayTask task = TaskUtil.newDelayTask(this::tryMath, 600);

            @Override
            public void insertUpdate(DocumentEvent e) {
                addTask();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                addTask();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                addTask();
            }

            private void addTask() {
                ThreadUtil.execAsync(() -> {
                    if (!"".equals(textArea.getText())) {
                        task.reset();

                        if (TaskUtil.containsTask(task)) {
                            TaskUtil.replaceTask(task);
                        } else {
                            TaskUtil.addTask(task);
                        }
                    }
                    matchResultField.setText("");
                });
            }

            private void tryMath() {
                String text = textArea.getText();
                if (text.matches(rule.getRule())) {
                    matchResultField.setForeground(new Color(77, 208, 13));
                    matchResultField.setText("验证成功，符合规则");
                } else {
                    matchResultField.setForeground(new Color(246, 10, 40));
                    matchResultField.setText("验证失败");
                }
            }


        });

        panel.setPreferredSize(new Dimension(400, 450));
        return panel;
    }

    @Data
    private static class Rule {
        private String title;
        private String rule;
        private List<String> examples;


        public void setRule(String rule) {
            if (rule.startsWith("/")) {
                rule = rule.substring(1);
            }
            if (rule.endsWith("/")) {
                rule = rule.substring(0, rule.length() - 1);
            }
            if (rule.endsWith("/i")) {
                // js是/i表示忽略大小写
                // java是(?i)表示忽略大小写
                rule = rule.substring(0, rule.length() - 2);
                if (rule.startsWith("^")) {
                    rule = "^(?i)" + rule.substring(1);
                } else {
                    rule = "(?i)" + rule;
                }
            }
            this.rule = rule;
        }


        @Override
        public String toString() {
            return title + "  " + rule;
        }
    }
}
