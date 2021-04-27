package com.developcollect.easycode.provider;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/26 17:04
 */
public class CommonCommandProvider implements Provider {

    @Override
    public int order() {
        return 6000;
    }

    @Override
    public String getTitle() {
        return "常用命令";
    }

    @Override
    public Component getComponent(JFrame frame) {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
        List<Command> commands = loadCommands();

        JList<Command> listView = new JList<>(UIUtil.toListModel(commands));
        JScrollPane jsp = new JScrollPane(listView);

        // 替换默认的复制操作
        listView.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (listView.getSelectedIndex() == -1) {
                    return;
                }
                Command selectedCommand = listView.getSelectedValue();
                UIUtil.setClipboardString(selectedCommand.getCmd());
            }
        });
//        listView.addKeyListener(new KeyAdapter() {
//
//            /**
//             * 对于按下键和释放键事件，getKeyCode 方法返回该事件的 keyCode。对于键入键事件，getKeyCode 方法总是返回 VK_UNDEFINED。
//             */
//            @Override
//            public void keyPressed(KeyEvent e) {
//                // 关键设置，当Ctrl+C组合键按下时响应
//                if ((e.getKeyCode() == KeyEvent.VK_C) && (e.isControlDown())) {
//                    if (listView.getSelectedIndex() == -1) {
//                        return;
//                    }
//                    Command selectedCommand = listView.getSelectedValue();
//                    // 目前不知道怎么覆盖swing自带的ctrl+c事件，只能通过延迟设置来覆盖粘贴板
//                    TaskUtil.addTask(new DelayTask(() -> {
//                        UIUtil.setClipboardString(selectedCommand.getCmd());
//                    }, 10));
//                }
//            }
//        });


        listView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listView.getSelectedIndex() == -1) {
                    return;
                }

                // 双击
                if (e.getClickCount() == 2) {
                    Command selectedCmd = listView.getSelectedValue();
                    // 弹出测试窗口
                    SDialog.builder()
                            .title("命令详情")
                            .parent(frame)
                            .body(cmdDetailPanel(selectedCmd))
                            .build()
                            .display();
                }
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
                    listView.setModel(UIUtil.toListModel(commands));
                    searched = false;
                } else {
                    if (searchField.isHint()) {
                        return;
                    }
                    List<Command> newList = commands.stream().filter(command -> command.title.contains(text)).collect(Collectors.toList());
                    listView.setModel(UIUtil.toListModel(newList));
                    searched = true;
                }
            }
        });


        UIUtil.bag(panel, searchField, 0, 1, 1, 0);
        UIUtil.bag(panel, jsp, 1, 1, 1, 1);
        return panel;
    }

    private JComponent cmdDetailPanel(Command selectedCmd) {
        JPanel panel = new JPanel(new BorderLayout());

        JTextPane textPane = UIUtil.onlyShowText(new JTextPane());
        textPane.setContentType("text/html");
        textPane.setText(buildHtmlStr(selectedCmd));
        JScrollPane scrollPane = new JScrollPane(textPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(800, 510));
        return panel;
    }


    private String buildHtmlStr(Command selectedCmd) {
        String title = selectedCmd.getTitle();
        title = title.replaceAll("^\\[.*?]", "");
        return "" +
                "<html>" +
                "<body style=\"padding-left: 20px;padding-right: 20px\">" +
                "<h1 style=\"margin: 0 auto; text-align: center\">" + title + "</h1>" +
                buildCmdHtml(selectedCmd, "Windows", Command::getWinCmd) +
                "<br/>" +
                buildCmdHtml(selectedCmd, "Linux", Command::getLinuxCmd) +
                "<br/>" +
                buildCmdHtml(selectedCmd, "Mac", Command::getMacCmd) +
                "<br/>" +
                "</body>" +
                "</html>";
    }

    private String buildCmdHtml(Command selectedCmd, String title, Function<Command, List<String>> func) {
        StringBuilder cmdHtml = new StringBuilder();
        List<String> list = func.apply(selectedCmd);
        if (list != null && !list.isEmpty()) {
            cmdHtml.append("<ul>");
            for (String s : list) {
                cmdHtml.append("<li>").append(s).append("</li>").append("\n");
            }
            cmdHtml.append("</ul>");
        }
        return "" +
                "<div>\n" +
                "    <h3 style=\"margin: 0\">" + title + "</h3>\n" +
                cmdHtml.toString() +
                "</div>";
    }

    private List<Command> loadCommands() {
        File rulesFile = new File(EASY_CODE_CONFIG.getHomeDir() + "/misc/commands.json");
        try {
            return JSON.parseArray(FileUtil.readAll(rulesFile), Command.class);
        } catch (Exception ignore) {
            JarFileUtil.copy("/misc/commands.json", EASY_CODE_CONFIG.getHomeDir() + "/misc/", RegexProvider.class);
            return JSON.parseArray(FileUtil.readAll(rulesFile), Command.class);
        }
    }


    @Data
    private static class Command {
        private String title;
        private List<String> winCmd;
        private List<String> linuxCmd;
        private List<String> macCmd;

        private static String getCmd(Command cmd, Function<Command, List<String>> func) {
            List<String> cmdList = func.apply(cmd);
            if (cmdList == null || cmdList.isEmpty()) {
                return "";
            } else {
                return cmdList.get(0);
            }
        }

        public String getCmd() {
            OsInfo osInfo = SystemUtil.getOsInfo();
            if (osInfo.isWindows()) {
                return getCmd(this, Command::getWinCmd);
            } else if (osInfo.isMac()) {
                return getCmd(this, Command::getMacCmd);
            } else {
                return getCmd(this, Command::getLinuxCmd);
            }
        }

        @Override
        public String toString() {
            return title + "  " + getCmd();
        }
    }
}
