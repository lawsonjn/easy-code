package com.developcollect.easycode.provider;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.developcollect.core.thread.ThreadUtil;
import com.developcollect.core.utils.NetUtil;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.ui.*;
import com.developcollect.easycode.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 13:57
 */
public class OtherProvider implements Provider {
    private static final Logger log = LoggerFactory.getLogger(OtherProvider.class);

    private Timer restReminderTimer = new Timer();

    @Override
    public int order() {
        return 15000;
    }

    private static String getMavenRepositoryDir() {
        boolean r1Empty = false;
        File userHomeSettingFile = new File(FileUtil.getUserHomePath() + "/.m2/settings.xml");
        String repository1 = readMavenRepositoryFromXml(userHomeSettingFile);
        if (StrUtil.isNotBlank(repository1) && FileUtil.isDirectory(repository1)) {
            // 如果目录为空，有可能用的是maven_home里的配置
            if (FileUtil.isEmpty(FileUtil.file(repository1))) {
                r1Empty = true;
            } else {
                return repository1;
            }
        }

        String mavenHome = System.getenv("MAVEN_HOME");
        if (StrUtil.isNotBlank(mavenHome)) {
            File mavenHomeSettingFile = new File(mavenHome + "/conf/settings.xml");
            String repository2 = readMavenRepositoryFromXml(mavenHomeSettingFile);
            if (StrUtil.isNotBlank(repository2) && FileUtil.isDirectory(repository2)) {
                if (FileUtil.isEmpty(FileUtil.file(repository2))) {
                    if (!r1Empty) {
                        return repository2;
                    }
                } else {
                    return repository2;
                }
            }
        }
        if (r1Empty) {
            return repository1;
        }
        return "";
    }

    private static String readMavenRepositoryFromXml(File xmlFile) {
        try {
            if (FileUtil.exist(xmlFile)) {
                Document document = XmlUtil.readXML(xmlFile);
                NodeList nodeList = document.getDocumentElement().getElementsByTagName("localRepository");
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception ignore) {
        }
        return "";
    }


    @Override
    public String getTitle() {
        return "其他";
    }

    @Override
    public Component getComponent(JFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));

        UIUtil.bag(panel, osInfoComponent(), 1, 5, 0, 0);
        UIUtil.bag(panel, portCloseComponent(frame), 1, 1, 0, 0);
        UIUtil.bag(panel, restComponent(), 1, 1, 0, 0);
        UIUtil.bag(panel, fileTypeComponent(frame), 1, 1, 0, 0);
        UIUtil.bag(panel, Box.createHorizontalGlue(), 0, 1, 0, 0);

        UIUtil.bag(panel, cleanMavenComponent(frame), 2, 1, 0, 0);
        UIUtil.bag(panel, ipLocationComponent(), 1, 2, 0, 0);
        UIUtil.bag(panel, Box.createHorizontalGlue(), 0, 2, 1, 0);

        UIUtil.bag(panel, jcgComponent(frame), 1, 2, 2, 6, 0, 0);

        UIUtil.bag(panel, ppp(), 1, 5, 0, 0);

        UIUtil.bag(panel, Box.createVerticalGlue(), 9, 1, 1, 0);
        UIUtil.bag(panel, Box.createVerticalGlue(), 0, 1, 1, 0);
        UIUtil.bag(panel, Box.createVerticalGlue(), 0, 1, 1, 1, new Insets(5, 5, 5, 5));

        return panel;
    }

    private Component osInfoComponent() {
        JPanel osInfoPanel = new JPanel(new GridBagLayout());
        osInfoPanel.setBorder(BorderFactory.createTitledBorder("系统信息"));
        JLabel intranetIpLabel = new JLabel("内网IP地址:");
        JTextField intranetIp = UIUtil.onlyShowText(new JTextField(NetUtil.localIpv4()));
        JLabel internetIpLabel = new JLabel("外网IP地址:");
        JTextField internetIp = UIUtil.onlyShowText(new JTextField(NetUtil.getInternetIp()));
        JLabel macLabel = new JLabel("MAC地址:");
        JTextField mac = UIUtil.onlyShowText(new JTextField(HardwareInfoUtil.getMacCode()));
        JLabel cpuSerialNumberLabel = new JLabel("CPU序列号:");
        JTextField cpuSerialNumber = UIUtil.onlyShowText(new JTextField(HardwareInfoUtil.getCpuCode()));
        JLabel diskSerialNumberLabel = new JLabel("硬盘序列号:");
        JTextField diskSerialNumber = UIUtil.onlyShowText(new JTextField(HardwareInfoUtil.getDiskCode()));

        UIUtil.bag(osInfoPanel, intranetIpLabel, 1, 1, 0, 0);
        UIUtil.bag(osInfoPanel, intranetIp, 0, 1, 0, 0);
        UIUtil.bag(osInfoPanel, internetIpLabel, 1, 1, 0, 0);
        UIUtil.bag(osInfoPanel, internetIp, 0, 1, 0, 0);
        UIUtil.bag(osInfoPanel, macLabel, 1, 1, 0, 0);
        UIUtil.bag(osInfoPanel, mac, 0, 1, 0, 0);
        UIUtil.bag(osInfoPanel, cpuSerialNumberLabel, 1, 1, 0, 0);
        UIUtil.bag(osInfoPanel, cpuSerialNumber, 0, 1, 0, 0);
        UIUtil.bag(osInfoPanel, diskSerialNumberLabel, 1, 1, 0, 0);
        UIUtil.bag(osInfoPanel, diskSerialNumber, 0, 1, 0, 0);
        UIUtil.bag(osInfoPanel, Box.createVerticalGlue(), 0, 1, 1, 1);


        osInfoPanel.setMinimumSize(new Dimension(230, 64));
        osInfoPanel.setPreferredSize(new Dimension(230, 64));
        return osInfoPanel;
    }

    private Component portCloseComponent(JFrame frame) {
        JPanel projectPanel = new JPanel(new GridBagLayout());
        projectPanel.setBorder(BorderFactory.createTitledBorder("解除端口占用"));
        JButton closeBtn = new JButton("关闭");
        JTextField portIn = new JTextField();
        addClosePortListener(portIn, closeBtn, frame);

        UIUtil.bag(projectPanel, portIn, 5, 1, 1, 1);
        UIUtil.bag(projectPanel, closeBtn, 0, 1, 0, 1);

        return standSize(projectPanel);
    }

    private void addClosePortListener(JTextField portIn, JButton closeBtn, JFrame frame) {
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String text = portIn.getText();
                if (StrUtil.isBlank(text)) {
                    return;
                }
                try {
                    int port = Integer.parseInt(text);
                    if (NetUtil.isValidPort(port)) {
                        if (NetUtil.isUsableLocalPort(port)) {
                            UIUtil.showInfo(StrUtil.format("端口[{}]未被占用", port));
                            return;
                        }
                        JTextArea ta = new JTextArea();
                        ta.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(ta);
                        scrollPane.setPreferredSize(new Dimension(650, 450));

                        SDialog.builder()
                                .title("解除端口占用")
                                .parent(frame)
                                .body(scrollPane)
                                .windowListener(new WindowAdapter() {
                                    @Override
                                    public void windowOpened(WindowEvent e) {
                                        String pid = null;
                                        // 查找端口所在进程
                                        // netstat -ano | findstr 8080
                                        String cmd = "cmd /c netstat -ano | findstr " + port;
                                        ta.append(cmd + "\n");
                                        List<String> resultLines = RuntimeUtil.execForLines(cmd);
                                        for (String resultLine : resultLines) {
                                            ta.append(resultLine + "\n");
                                            if (pid == null) {
                                                String[] split = resultLine.split(" +");
                                                if (split[2].endsWith(":" + port)) {
                                                    pid = split[5];
                                                }
                                            }
                                        }
                                        if (pid == null) {
                                            ta.append(StrUtil.format("未找到端口[{}]的占用进程", port) + "\n");
                                            return;
                                        }
                                        ta.append("\n");
                                        // 杀死进程
                                        String killCmd = "taskkill -F -PID " + pid;
                                        ta.append(killCmd + "\n");
                                        String str = RuntimeUtil.execForStr(killCmd);
                                        ta.append(str + "\n");
                                        if (str.startsWith("成功")) {
                                            ta.append(StrUtil.format("端口[{}]已解除占用", port) + "\n");
                                        } else {
                                            ta.append(StrUtil.format("端口[{}]解除占用失败", port) + "\n");
                                        }
                                    }
                                })
                                .build()
                                .display();
                    } else {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    UIUtil.showWarn("[" + text + "]不是一个合法端口");
                }
            }
        });
    }

    private Component restComponent() {
        JPanel restPanel = new JPanel(new GridBagLayout());
        restPanel.setBorder(BorderFactory.createTitledBorder("休息提醒"));
        JTextField restReminderIn = new SyncConfTextField(EASY_CODE_CONFIG::getRestReminderInterval);
        // 只能输入数字
        restReminderIn.setDocument(new PlainDocument() {
            @Override
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                try {
                    Integer.parseInt(str);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }

                super.insertString(offs, str, a);
            }
        });
        // 重新设置了document之后原来的值就没了，所以在重新设置一下
        restReminderIn.setText(String.valueOf(EASY_CODE_CONFIG.getRestReminderInterval()));
        JToggleButton restReminderTg = new SyncConfToggleButton(EASY_CODE_CONFIG::getRestReminder);
        addRestReminderListener(restReminderTg);


        UIUtil.bag(restPanel, restReminderIn, 5, 1, 1, 1);
        UIUtil.bag(restPanel, restReminderTg, 0, 1, 0, 1);
        return standSize(restPanel);
    }

    private void addRestReminderListener(JToggleButton toggleButton) {
        ItemListener itemListener = e -> {
            if (e == null || e.getStateChange() == ItemEvent.SELECTED) {
                restReminderTimer = new Timer();
                int restReminderInterval = EASY_CODE_CONFIG.getRestReminderInterval();
                restReminderTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        UIUtil.showRestReminder();
                    }
                }, restReminderInterval * 60000, restReminderInterval * 60000);
                if (e != null) {
                    UIUtil.showTip("已开启休息提醒！");
                }
            } else if (e.getStateChange() == ItemEvent.DESELECTED) {
                restReminderTimer.cancel();
                restReminderTimer = null;
                UIUtil.showTip("已关闭休息提醒！");
            }
        };


        toggleButton.addItemListener(itemListener);
        if (EASY_CODE_CONFIG.getRestReminder()) {
            itemListener.itemStateChanged(null);
        }

    }

    private Component cleanMavenComponent(Frame frame) {
        JPanel cleanMavenPanel = new JPanel(new GridBagLayout());
        cleanMavenPanel.setBorder(BorderFactory.createTitledBorder("清理Maven仓库"));
        SyncConfTextField mavenRepoIn = new SyncConfTextField(EASY_CODE_CONFIG::getOtMavenRepo);
        UIUtil.setFileChooserField(mavenRepoIn, "选择Maven仓库...", JFileChooser.DIRECTORIES_ONLY);
        String mavenRepositoryDir = getMavenRepositoryDir();
        if (StrUtil.isNotBlank(mavenRepositoryDir)) {
            mavenRepoIn.setHintText(mavenRepositoryDir);
        }

        JButton cleanBtn = new JButton("清理");

        UIUtil.bag(cleanMavenPanel, mavenRepoIn, 5, 1, 1, 1);
        UIUtil.bag(cleanMavenPanel, cleanBtn, 0, 1, 0, 1);


        cleanBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    String repoDir;
                    String repoInText = mavenRepoIn.getText();
                    if (StrUtil.isBlank(repoInText)) {
                        if (StrUtil.isNotBlank(mavenRepositoryDir)) {
                            repoDir = mavenRepositoryDir;
                        } else {
                            UIUtil.showInfo("请输入Maven仓库");
                            return;
                        }
                    } else {
                        if (FileUtil.isDirectory(repoInText)) {
                            repoDir = repoInText;
                        } else {
                            UIUtil.showWarn(frame, "输入的仓库无效");
                            return;
                        }
                    }


                    JTextArea ta = new JTextArea();
                    UIUtil.setMsgBoxAutoscroll(ta, true);
                    ta.setEditable(false);
                    SDialog.builder()
                            .title("清理Maven仓库")
                            .parent(frame)
                            .body(new JScrollPane(ta))
                            .size(1200, 1000)
                            .windowListener(new WindowAdapter() {
                                @Override
                                public void windowOpened(WindowEvent e) {
                                    ThreadUtil.execAsync(() -> {
                                        LineHandler lineHandler = line -> {
                                            ta.append(line);
                                            ta.append("\n");
                                        };
                                        EasyCodeUtil.clearMavenRepository(repoDir, lineHandler);
                                        lineHandler.handle("清理结束。");
                                    });
                                }
                            })
                            .build()
                            .display();
                } catch (Exception e1) {
                    UIUtil.showInfo("清理失败:" + e1.getMessage());
                }
            }
        });


        return cleanMavenPanel;
    }

    private Component jcgComponent(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("广告"));
        JTextField field = UIUtil.onlyShowText(new JTextField("广告位招租"));
        field.setHorizontalAlignment(JTextField.CENTER);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }



    private JComponent ipLocationComponent() {
        Box ipLocationPanel = Box.createVerticalBox();
        ipLocationPanel.setBorder(BorderFactory.createTitledBorder("IP定位"));

        HintTextField ipField = new HintTextField();
        HintTextField locationField = new HintTextField();
        locationField.setEditable(false);

        String internetIp = NetUtil.getInternetIp();
        if (StrUtil.isNotBlank(internetIp)) {
            ipField.setHintText(internetIp);
            String ipLocation = NetUtil.getIpLocation(internetIp);
            if (StrUtil.isNotBlank(ipLocation)) {
                locationField.setHintText(ipLocation);
            }
        }

        ipLocationPanel.add(ipField);
        ipLocationPanel.add(locationField);


        ipField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                getLocation();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                getLocation();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                getLocation();
            }

            private void getLocation() {
                String text = ipField.getText();
                try {
                    NetUtil.ipv4ToLong(text);
                    String location = NetUtil.getIpLocation(text);
                    locationField.setText(location);
                } catch (Exception e) {
                    locationField.setText("");
                }
            }
        });

        return standSize(ipLocationPanel, 1, 2);
    }


    private Component fileTypeComponent(JFrame frame) {
        JPanel fileTypePanel = new JPanel(new BorderLayout());
        fileTypePanel.setBorder(BorderFactory.createTitledBorder("文件类型识别"));
        JTextField fileTypeField = UIUtil.onlyShowText(new JTextField());
        fileTypeField.setHorizontalAlignment(JTextField.CENTER);
        fileTypePanel.add(fileTypeField, BorderLayout.CENTER);

        UIUtil.addDragListener(fileTypeField, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        List<File> list = (List<File>) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        File file = list.get(0);
                        String type = FileTypeUtil.getType(file);
                        if (StrUtil.isBlank(type)) {
                            type = "未知";
                        }
                        fileTypeField.setText(type);
                    } else {
                        dtde.rejectDrop();
                    }

                } catch (Exception e) {
                    UIUtil.showError(frame, "无法识别文件类型！");
                    log.error("无法识别文件类型", e);
                }
            }
        });
        return standSize(fileTypePanel);
    }


    private Component ppp() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("广告"));
        JTextField field = UIUtil.onlyShowText(new JTextField("广告位招租"));
        field.setHorizontalAlignment(JTextField.CENTER);
        panel.add(field, BorderLayout.CENTER);


        return panel;
    }


    private JComponent standSize(JComponent component) {
        return standSize(component, 1, 1);
    }


    private JComponent standSize(JComponent component, double w, double h) {
        component.setMinimumSize(new Dimension((int) (174 * w), (int) (64 * h)));
        component.setPreferredSize(new Dimension((int) (174 * w), (int) (64 * h)));
        return component;
    }

}
