package com.developcollect.easycode.ui;


import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.developcollect.core.swing.explorer.ExplorerUtil;
import com.developcollect.core.utils.FileUtil;
import com.developcollect.easycode.Provider;
import com.developcollect.easycode.core.db.DbInfo;
import com.developcollect.easycode.server.EasyCodeServer;
import com.developcollect.easycode.utils.*;
import com.melloware.jintellitype.JIntellitype;
import com.mysql.cj.jdbc.MysqlDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

import static com.developcollect.easycode.EasyCodeConfig.EASY_CODE_CONFIG;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/12 16:03
 */
@Slf4j
public class MainFrame extends JFrame {


    private static final String PRODUCT_NAME = "EasyCode";

    private ProvidersTabbedPane tabbedPane;
    private JPopupMenu trayPopupMenu;
    private JWindow trayWindow;
    private JPanel aboutPanel;
    private EasyCodeServer easyCodeServer;


    public MainFrame(JDialog startingDialog) {
        checkAndStartEasyCodeServer(startingDialog);
        initGlobalFont();
//        initToolbar();
        initMenu();
        initTabPane();
        initTray();
        initHotKey();
        initFrame(startingDialog);

        showUpdateTips();
        checkUpdate();
    }



    /**
     * 设置全局字体
     *
     * @author Zhu Kaixiao
     * @date 2020/5/7 10:16
     */
    private static void initGlobalFont() {
        FontUIResource fontUIResource = new FontUIResource(new Font("微软雅黑", Font.PLAIN, 14));
        for (Enumeration keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontUIResource);
            }
        }
    }

    /**
     * 注册系统级快捷键
     * 只支持windows
     */
    private void initHotKey() {
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.isWindows()) {
            //第一步：注册热键，第一个参数表示该热键的标识，第二个参数表示组合键，如果没有则为0，第三个参数为定义的主要热键
            JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT, 'E');

            //第二步：添加热键监听器
            JIntellitype.getInstance().addHotKeyListener(markCode -> {
                switch (markCode) {
                    case 1:
                        if (MainFrame.this.isActive()) {
                            MainFrame.this.dispose();
                        } else {
                            setExtendedState(Frame.NORMAL);
                            setVisible(true);
                        }
                        break;
                    default:
                        break;
                }
            });
        }


        // 应用内全局热键
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        // 注册应用程序全局键盘事件, 所有的键盘事件都会被此事件监听器处理.
        toolkit.addAWTEventListener(event -> {
                    if (event.getClass() == KeyEvent.class) {
                        KeyEvent kE = ((KeyEvent) event);
                        // 处理按键事件 ESC
                        if ((kE.getKeyCode() == KeyEvent.VK_ESCAPE)) {
                            dispose();
                            //window.refreshAction();
                        }
                    }
                }, java.awt.AWTEvent.KEY_EVENT_MASK);
    }

    private void initToolbar() {
        // 创建 一个工具栏实例
        JToolBar toolBar = new JToolBar("工具栏");

        // 创建 工具栏按钮
        JButton calcBtn = new JButton("计算器");

        calcBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Runtime.getRuntime().exec("calc");
                } catch (IOException ex) {
                }
            }
        });

        // 添加 按钮 到 工具栏
        toolBar.add(calcBtn);
        // 北方
        this.add(toolBar, BorderLayout.NORTH);
    }

    // region 菜单



    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        menuBar.add(fileMenu());
        menuBar.add(viewMenu());
        menuBar.add(helpMenu());


        this.setJMenuBar(menuBar);
    }

    private JMenu fileMenu() {
        JMenu menu = new JMenu("文件");
        menu.setMnemonic(KeyEvent.VK_F);    //设置快速访问符

        JMenuItem item = new JMenuItem("新建(N)", KeyEvent.VK_N);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tabbedPane.getSelectedProvider().create(e);
                } catch (Exception e1) {
                    log.error("执行新建出错: {}", tabbedPane.getSelectedProvider(), e1);
                }
            }
        });

        item = new JMenuItem("打开(O)", KeyEvent.VK_O);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tabbedPane.getSelectedProvider().open(e);
                } catch (Exception e1) {
                    log.error("执行打开出错: {}", tabbedPane.getSelectedProvider(), e1);
                }
            }
        });

        item = new JMenuItem("保存(S)", KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tabbedPane.getSelectedProvider().save(e);
                } catch (Exception e1) {
                    log.error("执行保存出错: {}", tabbedPane.getSelectedProvider(), e1);
                }
            }
        });

        item = new JMenuItem("另存为...", KeyEvent.VK_S);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.ALT_MASK));
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tabbedPane.getSelectedProvider().saveNew(e);
                } catch (Exception e1) {
                    log.error("执行另存为出错: {}", tabbedPane.getSelectedProvider(), e1);
                }
            }
        });

        menu.addSeparator();
        item = new JMenuItem("退出(X)", KeyEvent.VK_X);
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        return menu;
    }


    private JMenu viewMenu() {
        JMenu menu = new JMenu("设置");
        menu.setMnemonic(KeyEvent.VK_V);    //设置快速访问符




        JMenuItem item3 = new JMenuItem("打开工作目录");
        item3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        menu.add(item3);

        JMenuItem item2 = new JMenuItem("恢复出厂模板");
        menu.add(item2);

        menu.addSeparator();

        JMenuItem item = new JMenuItem("系统设置(T)", KeyEvent.VK_T);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        menu.add(item);
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SDialog.builder()
                        .title("系统设置")
                        .parent(MainFrame.this)
                        .body(settingPanel())
                        .build()
                        .display();
            }
        });


        item3.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(EASY_CODE_CONFIG.getHomeDir()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        item2.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 弹窗确认
                int confirm = UIUtil.showConfirm(MainFrame.this, "恢复出厂模板将会导致自定义模板丢失，是否继续？");
                if (confirm == 0) {
                    try {
                        FileUtil.del(EASY_CODE_CONFIG.getCodeTemplateHome());
                        EasyCodeUtil.releaseCodeTemplates();
                        UIUtil.showInfo("恢复成功！");
                    } catch (Exception ex) {
                        UIUtil.showInfo("恢复失败：" + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });

        return menu;

    }


    private JMenu helpMenu() {

        JMenuItem showLogInExplorer = new JMenuItem("在文件管理器中显示日志");
        showLogInExplorer.addActionListener(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File logFile = Util.getLogFile();
                if (!logFile.exists() || logFile.isDirectory()) {
                    UIUtil.showError("未找到日志文件");
                } else {
                    ExplorerUtil.select(logFile);
                }
            }
        });

        JMenuItem checkUpdate = new JMenuItem("检查更新");
        checkUpdate.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateUtil.hasUpdateAsync(b -> {
                    if (b) {
                        if (UIUtil.showConfirm(MainFrame.this, "发现新版本，是否立即更新？") == 0) {
                            UpdateUtil.doUpdate();
                        }
                    } else {
                        UIUtil.showInfo("您已经是最新版本啦~");
                    }
                });
            }
        });


        JMenuItem about = new JMenuItem("关于");
        aboutPanel = new JPanel();
        aboutPanel.add(UIUtil.vb(
                Box.createVerticalStrut(100),
                new JLabel("版　本:  " + BuildVersion.getVersion()),
                new JLabel("快捷键:  打开配置目录　　Shift + Ctrl + C"),
                new JLabel("　　　   呼出窗口　　　　Alt + E"),
                new JLabel("　　　   最小化窗口　　　Esc"),
                new JLabel("作　者:  zak"),
                new JLabel("邮　箱:  obiecana@foxmail.com")
        ));
        aboutPanel.setPreferredSize(new Dimension(400, 450));
        about.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SDialog.builder()
                        .title("关于")
                        .parent(MainFrame.this)
                        .body(aboutPanel)
                        .build()
                        .display();
            }
        });

        JMenu help = new JMenu("帮助");
        help.add(showLogInExplorer);
        help.addSeparator();
        help.add(checkUpdate);
        help.add(about);
        return help;
    }

    // endregion


    /**
     * 初始化主界面
     */
    private void initTabPane() {
        // 添加选项卡
        tabbedPane = new ProvidersTabbedPane(MainFrame.this);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);

        // 通过SPI机制发现Provider
        List<Provider> providers = new LinkedList<>();
        ServiceLoader<Provider> serviceLoader = ServiceLoader.load(Provider.class, ClassLoaderUtil.getClassLoader());
        Iterator<Provider> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                providers.add(iterator.next());
            } catch (Exception e) {
                log.error("Provider加载失败");
            }
        }

        providers.sort(Comparator.naturalOrder());
        for (Provider provider : providers) {
            try {
                if (!provider.enable()) {
                    log.info("Provider[{}]已禁用", provider.getClass());
                    continue;
                }
                tabbedPane.addProvider(provider);
            } catch (Exception e) {
                log.error("Provider[{}]加载失败", provider.getClass(), e);
            }
        }


        // 添加选项卡面板
        this.add(tabbedPane, BorderLayout.CENTER);

//        // 添加监听器
//        tabbedPane.addChangeListener(new ChangeListener() {
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                int n = tabbedPane.getSelectedIndex();
//                loadTab(n);
//            }
//        });
    }


    /**
     * 初始化托盘
     */
    private void initTray() {
        if (SystemTray.isSupported()) {
            // 定义托盘图标的图片
//            String path = ClassLoader.getSystemResource("ic16.png").getFile();
//            Image image = Toolkit.getDefaultToolkit().getImage(path);
            // 创建托盘图标
//            UIUtil.getImageIcon("ic16.png")
            TrayIcon trayIcon = new TrayIcon(UIUtil.getImage("img/ic16.png"));


            // 为托盘添加鼠标适配器
            trayIcon.addMouseListener(new MouseAdapter() {
                // 鼠标事件
                @Override
                public void mouseClicked(MouseEvent e) {

//                    if (e.getButton() == MouseEvent.BUTTON3) {
//                        trayWindow.setLocation(e.getX() - 80, e.getY() - 80);
//                        trayWindow.setVisible(true);
//                        trayPopupMenu.show(trayWindow, 0, 0);
//                    }
                    // 判断是否双击了鼠标
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        setExtendedState(Frame.NORMAL);
                        setVisible(true);
                    }
                }
            });


            this.addWindowListener(new WindowAdapter() {
                private volatile int flg;

                @Override
                public void windowActivated(WindowEvent e) {
                    flg = 0;
                }

                @Override
                public void windowIconified(WindowEvent e) {
                    if (flg == 0) {
                        dispose();
                        flg = 2;
                    }
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    if (flg == 0) {
                        flg = 1;
                    }
                }

            });
            // 添加工具提示文本
            trayIcon.setToolTip("Easy Code\r\n状态：假装有个状态");
            // 创建弹出菜单
            trayPopupMenu = new JPopupMenu();
            trayPopupMenu.add(new JMenuItem("Show"));
            trayPopupMenu.addSeparator();
            trayPopupMenu.add(new JMenuItem("Exit"));
            trayWindow = new JWindow();
            trayWindow.setBackground(new Color(229, 244, 227));
            trayWindow.setSize(10, 10);
//            trayPopupMe

            trayPopupMenu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    trayWindow.dispose();
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    trayWindow.dispose();
                }
            });


            // 为托盘图标加弹出菜弹
            PopupMenu pm = new PopupMenu();
            pm.add("Show");
            pm.addSeparator();
            pm.add("Exit");
            pm.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getID() == ActionEvent.ACTION_PERFORMED) {
                        switch (e.getActionCommand()) {
                            case "Show":
                                MainFrame.this.setExtendedState(Frame.NORMAL);
                                MainFrame.this.setVisible(true);
                                break;
                            case "Exit":
                                System.exit(0);
                                break;
                        }
                    }
                }
            });
            trayIcon.setPopupMenu(pm);

            // 获得系统托盘对象
            SystemTray systemTray = SystemTray.getSystemTray();
            try {
                // 为系统托盘加托盘图标
                systemTray.add(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            UIUtil.showTip("不支持系统托盘");
        }
    }


    private void initFrame(JDialog startingDialog) {
        this.setBounds(0, 0, 800, 510);
        this.setLocationRelativeTo(null);
        this.setTitle(PRODUCT_NAME + " " + BuildVersion.getVersion());
        this.setIconImage(UIUtil.getImage("img/ic16.png"));
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (EASY_CODE_CONFIG.getMinimizeOnClose()) {
                    MainFrame.this.dispose();
                } else {
                    System.exit(1);
                }
            }
        });
//        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startingDialog.dispose();
        this.setVisible(true);

    }


    private void checkUpdate() {
        ThreadUtil.execAsync(new Runnable() {
            @Override
            public void run() {
                if (UpdateUtil.hasUpdate()) {
                    if (UIUtil.showConfirm(MainFrame.this, "发现新版本，是否立即更新？") == 0) {
                        UpdateUtil.doUpdate();
                    } else {
                        return;
                    }
                }

                synchronized (this) {
                    try {
                        this.wait(15 * 60 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, true);
    }


    private void showUpdateTips() {
        if (EASY_CODE_CONFIG.getLastRunVersion().equals(BuildVersion.getVersion())) {
            return;
        }
        List<String> tips = UpdateUtil.getUpdateTips();
        if (!tips.isEmpty()) {
            Box vBox = Box.createVerticalBox();
            vBox.add(Box.createVerticalStrut(80));
            vBox.add(new JLabel("本次更新版本：" + tips.get(0) + "                           "));
            for (int i = 1; i < tips.size(); i++) {
                vBox.add(new JLabel(tips.get(i)));
            }
            JPanel panel = new JPanel();
            panel.add(vBox);
            panel.setPreferredSize(new Dimension(400, 450));

            SDialog.builder()
                    .title("更新")
                    .parent(MainFrame.this)
                    .body(panel)
                    .build()
                    .display();
        }
        EASY_CODE_CONFIG.setLastRunVersion(BuildVersion.getVersion());
    }


    private void checkAndStartEasyCodeServer(JDialog startingDialog) {
        if (EasyCodeServer.checkStarted()) {
            startingDialog.dispose();
            String[] options = {"确定"};
            JOptionPane.showOptionDialog(null, "禁止重复启动，已启动的EasyCode可用【Alt+e】快速唤醒", "提示",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            System.exit(1);
        } else {
            easyCodeServer = EasyCodeServer.bind();
        }
    }


    private JComponent settingPanel() {
        JSplitPane panel = new JSplitPane();

        // 右边
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.add("基本设置", basicSetting());
        contentPanel.add("数据库", dbSetting());
        contentPanel.add("扩展设置", extraSetting());



        // 左边
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        DefaultMutableTreeNode basicNode = new DefaultMutableTreeNode("基本设置");
        DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode("数据库");
        DefaultMutableTreeNode extraNode = new DefaultMutableTreeNode("扩展设置");
        root.add(basicNode);
        root.add(dbNode);
        root.add(extraNode);
        JTree tree = new JTree(new DefaultTreeModel(root));
        // 更改默认的JTree图标
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
//        Icon openIcon = new ImageIcon("open.png");
        Icon originClosedIcon = renderer.getClosedIcon();
        renderer.setClosedIcon(UIUtil.getImageIcon("img/arrow-r.png", originClosedIcon.getIconWidth(), originClosedIcon.getIconHeight()));
        renderer.setOpenIcon(UIUtil.getImageIcon("img/arrow-d.png", originClosedIcon.getIconWidth(), originClosedIcon.getIconHeight()));
        renderer.setLeafIcon(UIUtil.getImageIcon("img/invisible.png", originClosedIcon.getIconWidth(), originClosedIcon.getIconHeight()));
        tree.setSelectionInterval(0, 1);

        JScrollPane treeScrollPane = new JScrollPane(tree);
        tree.setRootVisible(false);
        treeScrollPane.setMinimumSize(new Dimension(180, 180));
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                String lastPathComponent = e.getNewLeadSelectionPath().getLastPathComponent().toString();
                cardLayout.show(contentPanel, lastPathComponent);
            }
        });



        // 隐藏分割线
//        SplitPaneUI ui = panel.getUI();
//        if (ui instanceof BasicSplitPaneUI) {
//            ((BasicSplitPaneUI) ui).getDivider().setBorder(null);
//        }
        panel.setOneTouchExpandable(false);//让分割线显示出箭头
        panel.setContinuousLayout(false);//操作箭头，重绘图形
        panel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);//设置分割线方向
//        panel.setB
        panel.setLeftComponent(treeScrollPane);//布局中添加组件 ，面板1
        panel.setRightComponent(contentPanel);//添加面板2
        panel.setDividerSize(1);//设置分割线的宽度
        //jSplitPane.setDividerLocation(100);//设置分割线位于中央
        panel.setDividerLocation(180);//设定分割线的距离左边的位置
        panel.setMinimumSize(new Dimension(750, 450));
        panel.setPreferredSize(panel.getMinimumSize());
        return panel;
    }

    private Component dbSetting() {
        JList<DbInfo> dbInfoList = new JList<>(UIUtil.toListModel(EASY_CODE_CONFIG.getDbInfos()));
        JPopupMenu jPopupMenu = new JPopupMenu();
        JMenuItem delItem = new JMenuItem("删除");
        jPopupMenu.add(delItem);

        delItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = dbInfoList.getSelectedIndex();
                EASY_CODE_CONFIG.getDbInfos().remove(selectedIndex);
                dbInfoList.setModel(UIUtil.toListModel(EASY_CODE_CONFIG.getDbInfos()));
            }
        });

        dbInfoList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedIndex = dbInfoList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    if (e.getButton() == 3) {
                        jPopupMenu.show(dbInfoList, e.getX(),e.getY());
                    } else if (e.getButton() == 1 && e.getClickCount() == 2) {
                        showDbInfoEditPanel(dbInfoList, selectedIndex);
                    }
                }
            }
        });

        JButton addBtn = new JButton("添加");
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDbInfoEditPanel(dbInfoList, -1);
            }
        });


        JPanel panel = new JPanel(new GridBagLayout());
        UIUtil.bag(panel, new JScrollPane(dbInfoList), 0, 1, 1, 1);
        UIUtil.bag(panel, addBtn, 1, 1, 0, 0);

        return panel;
    }


    private void showDbInfoEditPanel(JList<DbInfo> dbInfoList, int idx) {
        JPanel addDbInfoPanel = new JPanel(new GridBagLayout());
        JLabel dbUrl = new JLabel("URL：", SwingConstants.RIGHT);
        JTextField dbUrlIn = new JTextField();

        JLabel dbUsername = new JLabel("　用户名：", SwingConstants.RIGHT);
        JTextField dbUsernameIn = new JTextField();

        JLabel dbPassword = new JLabel("　密码：", SwingConstants.RIGHT);
        JTextField dbPasswordIn = new JTextField();

        if (idx > -1) {
            DbInfo element = dbInfoList.getModel().getElementAt(idx);
            dbUrlIn.setText(element.getUrl());
            dbUsernameIn.setText(element.getUsername());
            dbPasswordIn.setText(element.getPassword());
        }


        UIUtil.bag(addDbInfoPanel, dbUrl,          1, 1, 0, 0);
        UIUtil.bag(addDbInfoPanel, dbUrlIn,        8, 1, 1, 0);
        UIUtil.bag(addDbInfoPanel, Box.createHorizontalGlue(), 0, 1, 0, 0);
        UIUtil.bag(addDbInfoPanel, dbUsername,     1, 1, 0, 0);
        UIUtil.bag(addDbInfoPanel, dbUsernameIn,   8, 1, 1, 0);
        UIUtil.bag(addDbInfoPanel, Box.createHorizontalGlue(), 0, 1, 0, 0);
        UIUtil.bag(addDbInfoPanel, dbPassword,     1, 1, 0, 0);
        UIUtil.bag(addDbInfoPanel, dbPasswordIn,   8, 1, 1, 0);
        UIUtil.bag(addDbInfoPanel, Box.createHorizontalGlue(), 0, 1, 0, 0);

        addDbInfoPanel.setPreferredSize(new Dimension(600, 300));

        SDialog.builder()
                .title("新增数据库连接信息")
                .body(addDbInfoPanel)
                .foot("连接测试", "-", "确定", "取消")
                .parent(MainFrame.this)
                .resizable(false)
                .footMouseListener(new SDialog.FootMouseListener() {
                    @Override
                    public void mouseClicked(SDialog.FootMouseEvent footMouseEvent) {
                        // 确定
                        if (footMouseEvent.getFootIdx() == 2) {
                            DbInfo dbInfo = new DbInfo();
                            dbInfo.setUrl(dbUrlIn.getText());
                            dbInfo.setUsername(dbUsernameIn.getText());
                            dbInfo.setPassword(dbPasswordIn.getText());
                            if (idx < 0) {
                                EASY_CODE_CONFIG.getDbInfos().add(dbInfo);
                            } else {
                                EASY_CODE_CONFIG.getDbInfos().set(idx, dbInfo);
                            }
                            dbInfoList.setModel(UIUtil.toListModel(EASY_CODE_CONFIG.getDbInfos()));
                            SDialog source = footMouseEvent.getSource();
                            source.dispose();
                        } else if (footMouseEvent.getFootIdx() == 0) {
                            SQLException throwables = testDbInfo(dbUrlIn.getText(), dbUsernameIn.getText(), dbPasswordIn.getText());
                            if (throwables == null) {
                                UIUtil.showInfo("连接成功");
                            } else {
                                UIUtil.showInfo("连接失败：" + throwables.getMessage());
                            }
                        } else {
                            SDialog source = footMouseEvent.getSource();
                            source.dispose();
                        }
                    }

                    private SQLException testDbInfo(String url, String username, String password) {
                        try {
                            MysqlDataSource mysqlDataSource = new MysqlDataSource();
                            if (StringUtils.startsWithIgnoreCase(url, "JDBC:MYSQL") && !url.contains("?")) {
                                url = url + "?useUnicode=true&useSSL=false&characterEncoding=utf8&serverTimezone=GMT%2B8";
                            }
                            mysqlDataSource.setURL(url);
                            mysqlDataSource.setUser(username);
                            mysqlDataSource.setPassword(password);
                            Connection connection = mysqlDataSource.getConnection();
                            connection.close();
                            return null;
                        } catch (SQLException t) {
                            return t;
                        }
                    }
                })
                .build()
                .display();
    }







    private JPanel basicSetting() {

        // 生成文件保存位置
        JLabel productLabel = new JLabel("保存路径：", SwingConstants.RIGHT);
        SyncConfTextField productDirIn = new SyncConfTextField(EASY_CODE_CONFIG::getProductDir);
        UIUtil.setFileChooserField(productDirIn, "选择保存路径", JFileChooser.DIRECTORIES_ONLY);

        // Maven路径
        JLabel qdMavenHome = new JLabel("Maven路径：", SwingConstants.RIGHT);
        SyncConfTextField mavenHomeIn = new SyncConfTextField(EASY_CODE_CONFIG::getQdMavenHome);
        UIUtil.setFileChooserField(mavenHomeIn, "选择Maven路径", JFileChooser.DIRECTORIES_ONLY);

        JLabel minimizeOnCloseLabel = new JLabel("关闭时最小化：", SwingConstants.RIGHT);
        SyncConfCheckBox minimizeOnCloseCheckBox = new SyncConfCheckBox(EASY_CODE_CONFIG::getMinimizeOnClose);

        JPanel panel = new JPanel(new GridBagLayout());

        UIUtil.bag(panel, productLabel, 1, 1, 0, 0);
        UIUtil.bag(panel, productDirIn, 0, 1, 1, 0);

        UIUtil.bag(panel, qdMavenHome, 1, 1, 0, 0);
        UIUtil.bag(panel, mavenHomeIn, 0, 1, 1, 0);

        UIUtil.bag(panel, minimizeOnCloseLabel, 1, 1, 0, 0);
        UIUtil.bag(panel, minimizeOnCloseCheckBox, 0, 1, 1, 0);

        UIUtil.bag(panel, Box.createVerticalGlue(), 2, 1, 1, 1);

        return panel;
    }


    private JPanel extraSetting() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("广告"));
        JTextField field = UIUtil.onlyShowText(new JTextField("广告位招租"));
        field.setHorizontalAlignment(JTextField.CENTER);
        panel.add(field, BorderLayout.CENTER);


        return panel;
    }
}
