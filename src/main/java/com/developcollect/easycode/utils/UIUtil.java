package com.developcollect.easycode.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.developcollect.core.task.DelayTask;
import com.developcollect.core.task.TaskUtil;
import com.developcollect.core.thread.ThreadUtil;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.core.utils.ReflectUtil;
import com.developcollect.core.utils.StrUtil;
import com.developcollect.easycode.ui.AttachField;
import com.developcollect.easycode.ui.IconTextField;
import com.developcollect.easycode.ui.InputPauseListener;
import com.developcollect.easycode.ui.ToolTip;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


//  this.setExtendedState(JFrame.MAXIMIZED_BOTH); //最大化
//    this.setAlwaysOnTop(true);    //总在最前面
//    this.setResizable(false);    //不能改变大小
//    this.setUndecorated(true);    //不要边框
//
//    接下来是事件部分,需要关心的事件是windowLostFocus和windowIconified(最小化)
//
//    public void this_windowLostFocus(WindowEvent e) {
//        this.requestFocus();
//        this.setLocation(0,0);
//    }
//
//    public void this_windowIconified(WindowEvent e) {
//        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
//    }


/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/13 16:29
 */
public class UIUtil {

    private static final GridBagConstraints gbc;

    static {
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 1, 1, 1);
    }

    public static IconTextField setFileChooserField(IconTextField iconTextField) {
        return setFileChooserField(iconTextField, "选择文件...", JFileChooser.FILES_AND_DIRECTORIES, LambdaUtil::nop, Objects::nonNull);
    }

    public static IconTextField setFileChooserField(IconTextField iconTextField, String title) {
        return setFileChooserField(iconTextField, title, JFileChooser.FILES_AND_DIRECTORIES, LambdaUtil::nop, Objects::nonNull);
    }

    public static IconTextField setFileChooserField(IconTextField iconTextField, String title, String... extensions) {
        return setFileChooserField(iconTextField, title, JFileChooser.FILES_ONLY, LambdaUtil::nop, Objects::nonNull, extensions);
    }

    public static IconTextField setFileChooserField(IconTextField iconTextField, String title, int mode, String... extensions) {
        return setFileChooserField(iconTextField, title, mode, LambdaUtil::nop, Objects::nonNull, extensions);
    }

    public static IconTextField setFileChooserField(
            IconTextField field, String title, int mode, Consumer<File> consumer, Function<File, Boolean> inspect, String... extensions) {
        field.setIcon(UIUtil.getImageIcon("img/fo1.png"));
        field.setPlace(IconTextField.PLACE_RIGHT);
        field.addIconMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                field.setIcon(UIUtil.getImageIcon("img/fo2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                field.setIcon(UIUtil.getImageIcon("img/fo1.png"));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jfc = new JFileChooser();
                String oldPath = field.getText();
                if (FileUtil.exist(oldPath)) {
                    File oldp = new File(oldPath);
//                    File selectedFile = oldp;
                    if (oldp.isDirectory()) {
                        oldp = oldp.getParentFile();
                    }
                    jfc.setCurrentDirectory(oldp);
//                    jfc.setSelectedFile(selectedFile);
                }


                jfc.setPreferredSize(new Dimension(1100, 750));
                jfc.setDialogTitle(title);
                jfc.setFileSelectionMode(mode);
                if (extensions != null && extensions.length != 0) {
                    FileFilter[] choosableFileFilters = jfc.getChoosableFileFilters();
                    for (FileFilter choosableFileFilter : choosableFileFilters) {
                        jfc.removeChoosableFileFilter(choosableFileFilter);
                    }

                    jfc.addChoosableFileFilter(new FileNameExtensionFilter(StrUtil.join(", ", extensions), extensions));
                }

                jfc.showDialog(field.getParent(), "选择");
                File file = jfc.getSelectedFile();
                if (file == null) {
                    return;
                }
                if (!inspect.apply(file)) {
                    return;
                }
                String absolutePath = file.getAbsolutePath();
                field.setText(absolutePath);
                consumer.accept(file);
            }
        });
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                field.setIcon(UIUtil.getImageIcon("img/fo1.png"));
            }
        });
        return field;
    }


    public static void addFileChooser(JTextField textIn, JButton btn, String title, int mode, Consumer<File> consumer, Function<File, Boolean> inspect) {
        btn.setFocusPainted(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jfc = new JFileChooser();
                String oldPath = textIn.getText();
                if (FileUtil.exist(oldPath)) {
                    File oldp = new File(oldPath);
//                    File selectedFile = oldp;
                    if (oldp.isDirectory()) {
                        oldp = oldp.getParentFile();
                    }
                    jfc.setCurrentDirectory(oldp);
//                    jfc.setSelectedFile(selectedFile);
                }

                jfc.setPreferredSize(new Dimension(1100, 750));
                jfc.setDialogTitle(title);
                jfc.setFileSelectionMode(mode);
                jfc.showDialog(new JLabel(), "选择");
                File file = jfc.getSelectedFile();
                if (file == null) {
                    return;
                }
                if (!inspect.apply(file)) {
                    return;
                }
                String absolutePath = file.getAbsolutePath();
                textIn.setText(absolutePath);
                consumer.accept(file);
            }

            @Override
            public void mouseEntered(MouseEvent me) {
                if (btn.isEnabled()) {
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
//                    btn.setForeground(Color.RED);
                }
            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (btn.isEnabled()) {
                    btn.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//                    btn.setForeground(Color.BLACK);
                }
            }


            @Override
            public void mousePressed(MouseEvent me) {
//                if (btn.isEnabled()) {
//                    btn.setForeground(Color.CYAN);
//                }
            }


            @Override
            public void mouseReleased(MouseEvent me) {
//                if (btn.isEnabled()) {
//                    btn.setForeground(Color.BLACK);
//                }
            }
        });
    }

    public static void addFileChooser(JTextField textIn, JButton btn, String title, int mode) {
        addFileChooser(textIn, btn, title, mode, LambdaUtil::nop, Objects::nonNull);
    }


    public static void setSearchField(IconTextField field) {
        field.setHintText("输入文字搜索...");
        field.setIcon(getImageIcon("img/search.png"));
    }

    public static Box hb(Component... components) {
        Box hb1 = Box.createHorizontalBox();
        for (Component component : components) {
            hb1.add(component);
        }
        return hb1;
    }

    public static Box vb(Component... components) {
        Box hb1 = Box.createVerticalBox();
        for (Component component : components) {
            hb1.add(component);
        }
        return hb1;
    }


    public static void bag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill, Insets insets) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        //该方法是设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        //该方法设置组件水平的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间, 或者0到100之间 也就是多余空间获取的比例
        gbc.weightx = weightx;
        //该方法设置组件垂直的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
        gbc.weighty = weighty;
        gbc.fill = fill;
        if (insets != null) {
            gbc.insets = insets;
        }

        LayoutManager layout = container.getLayout();
        if (!(layout instanceof GridBagLayout)) {
            if (container instanceof RootPaneContainer) {
                RootPaneContainer rc = (RootPaneContainer) container;
                layout = rc.getContentPane().getLayout();
            }
        }
        if (!(layout instanceof GridBagLayout)) {
            throw new IllegalStateException("容器布局不是GridBagLayout");
        }
        GridBagLayout gbl = (GridBagLayout) layout;
        container.add(component);
        gbl.setConstraints(component, gbc);
    }

    public static void bag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, int fill) {
        bag(container, component, gridx, gridy, gridwidth, gridheight, weightx, weighty, fill, null);
    }

    public static void bag(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty) {
        bag(container, component, gridx, gridy, gridwidth, gridheight, weightx, weighty, GridBagConstraints.BOTH);
    }

    public static void bag(Container container, Component component, int gridwidth, int gridheight, double weightx, double weighty, int fill, Insets insets) {
        bag(container, component, -1, -1, gridwidth, gridheight, weightx, weighty, fill, insets);
    }

    public static void bag(Container container, Component component, int gridwidth, int gridheight, double weightx, double weighty, int fill) {
        bag(container, component, gridwidth, gridheight, weightx, weighty, fill, null);
    }

    public static void bag(Container container, Component component, int gridwidth, int gridheight, double weightx, double weighty) {
        bag(container, component, gridwidth, gridheight, weightx, weighty, GridBagConstraints.BOTH);
    }

    public static void bag(Container container, Component component, int gridwidth, int gridheight, double weightx, double weighty, Insets insets) {
        bag(container, component, gridwidth, gridheight, weightx, weighty, GridBagConstraints.BOTH, insets);
    }


    public static ImageIcon getImageIcon(String path, int width, int height) {
        if (width == 0 || height == 0) {
            return getImageIcon(path);
        }
        ImageIcon icon = new ImageIcon(getImage(path, width, height));
        return icon;
    }

    public static ImageIcon getImageIcon(String path, int width, int height, int degree) {
        if (width == 0 || height == 0) {
            return getImageIcon(path);
        }
        ImageIcon icon = new ImageIcon(getImage(path, width, height, degree));
        return icon;
    }

    public static ImageIcon getImageIcon(String path) {
        return new ImageIcon(UIUtil.class.getClassLoader().getResource(path));
    }

    public static Image getImage(String path, int width, int height) {
        return getImage(path, width, height, 0);
    }

    public static Image getImage(String path, int width, int height, int degree) {
        BufferedImage bufferedImage = ImgUtil.read(UIUtil.class.getClassLoader().getResource(path));
        Image scaledInstance = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        if (degree > 0) {
            return ImgUtil.rotate(scaledInstance, degree % 360);
        }

        return scaledInstance;
    }

    public static Image zoomImage(Image image, int width, int height) {
        return image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public static Image getImage(String path) {
        return ImgUtil.read(UIUtil.class.getClassLoader().getResource(path));
    }


    public static ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    private static final JFrame REST_REMINDER_FRAME = new JFrame();

    static {

        REST_REMINDER_FRAME.setSize(10, 10);
        REST_REMINDER_FRAME.setLayout(new GridBagLayout());


        REST_REMINDER_FRAME.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                REST_REMINDER_FRAME.requestFocus();
                REST_REMINDER_FRAME.setLocation(0, 0);
            }
        });

        REST_REMINDER_FRAME.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                REST_REMINDER_FRAME.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        });


        JLabel lab = new JLabel("休息一会儿吧~");
        lab.setFont(new Font("微软雅黑", Font.PLAIN, 100));
        JButton b = new JButton("取消");
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                REST_REMINDER_FRAME.dispose();
            }
        });


        UIUtil.bag(REST_REMINDER_FRAME, lab, 0, 1, 0, 0, GridBagConstraints.NONE);
        UIUtil.bag(REST_REMINDER_FRAME, b, 0, 1, 0, 0, GridBagConstraints.NONE);


        REST_REMINDER_FRAME.setExtendedState(JFrame.MAXIMIZED_BOTH); //最大化
        REST_REMINDER_FRAME.setAlwaysOnTop(true);    //总在最前面
        REST_REMINDER_FRAME.setResizable(false);    //不能改变大小
        REST_REMINDER_FRAME.setUndecorated(true);    //不要边框

    }

    public static void showRestReminder() {
        REST_REMINDER_FRAME.setExtendedState(JFrame.MAXIMIZED_BOTH);
        REST_REMINDER_FRAME.setVisible(true);
        REST_REMINDER_FRAME.setAlwaysOnTop(true);
        REST_REMINDER_FRAME.setOpacity(0.4f);
    }

    public static void showTip(String msg, Object... objs) {
        ToolTip.showTip(msg, objs);
    }

    public static void showTip(ImageIcon icon, String msg, Object... objs) {
        ToolTip.showTip(icon, msg, objs);
    }

    public static void showInfo(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);

    }

    public static void showError(Component parentComponent, String msg) {
        JOptionPane.showMessageDialog(parentComponent, msg, "错误", JOptionPane.ERROR_MESSAGE);

    }

    public static void showWaitError(String message) {
        showWaitError(null, message);
    }

    public static void showWaitError(Component parent, String message) {
        String[] options = {"确定"};
        JOptionPane.showOptionDialog(parent, message, "错误",
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }


    public static void showWarn(String msg) {
        showWarn("警告", msg);
    }

    public static void showWarn(Component parentComponent, String msg) {
        showWarn(parentComponent, "警告", msg);
    }

    public static void showWarn(String title, String msg) {
        showWarn(null, title, msg);
    }

    public static void showWarn(Component parentComponent, String title, String msg) {
        JOptionPane.showMessageDialog(parentComponent, msg, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showQuestion(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.QUESTION_MESSAGE);
    }

    public static void showPlain(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void showInfo(String msg) {
        showInfo("提示", msg);
    }

    public static void showError(String msg) {
        showError("错误", msg);
    }


    public static void showWaitInfo(Component parent, String message) {
        String[] options = {"确定"};
        JOptionPane.showOptionDialog(parent, message, "提示",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    public static void showQuestion(String msg) {
        showQuestion("提示", msg);
    }

    public static void showPlain(String msg) {
        showPlain("提示", msg);
    }

    public static int showConfirm(Component parentComponent, Object message) {
        return JOptionPane.showConfirmDialog(parentComponent, message, "提示", JOptionPane.YES_NO_OPTION);
    }

    public static void addDragListener(Component c, DropTargetAdapter adapter) {
        new DropTarget(c, DnDConstants.ACTION_COPY_OR_MOVE, adapter);
    }

    public static File showSaveFileChooser() {
        return showSaveFileChooser(null, null, null, null);
    }

    public static File showSaveFileChooser(String[] extensions) {
        return showSaveFileChooser(null, null, null, extensions);
    }

    public static File showSaveFileChooser(String defaultFilename, String[] extensions) {
        return showSaveFileChooser(null, null, defaultFilename, extensions);
    }

    public static File showSaveFileChooser(String basePath, String defaultFilename, String[] extensions) {
        return showSaveFileChooser(null, basePath, defaultFilename, extensions);
    }

    public static File showSaveFileChooser(String title, String basePath, String defaultFilename, String[] extensions) {
        JFileChooser chooser = new JFileChooser();
        if (StrUtil.isNotBlank(basePath)) {
            chooser.setCurrentDirectory(new File(basePath));
        }
        if (StrUtil.isBlank(title)) {
            title = "保存文件";
        }
        chooser.setDialogTitle(title);
        if (StrUtil.isBlank(defaultFilename)) {
            defaultFilename = "未命名";
        }

        if (extensions != null && extensions.length != 0) {
            FileFilter[] choosableFileFilters = chooser.getChoosableFileFilters();
            for (FileFilter choosableFileFilter : choosableFileFilters) {
                chooser.removeChoosableFileFilter(choosableFileFilter);
            }
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(StrUtil.join(", ", extensions), extensions));

            boolean defaultFilenameHasExt = false;
            for (String extension : extensions) {
                if (defaultFilename.endsWith("." + extension)) {
                    defaultFilenameHasExt = true;
                    break;
                }
            }
            if (!defaultFilenameHasExt) {
                defaultFilename = defaultFilename + "." + extensions[0];
            }
        }
        chooser.setSelectedFile(new File(defaultFilename)); //设置默认文件名
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        //chooser.setDialogType(JFileChooser.OPEN_DIALOG);//设置为“打开”
        //chooser.setApproveButtonText("保存");//设置按钮上的文字，默认是“保存”或者“打开”
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);//仅显示目录；有三种，仅文件，仅目录，两者都
        chooser.setPreferredSize(new Dimension(1100, 750));
        chooser.showSaveDialog(null);//show”保存“
        //chooser.showOpenDialog(null);//show“打开”

        return chooser.getSelectedFile();
    }

    public static File showFileChooser() {
        return showFileChooser(null, null, JFileChooser.FILES_ONLY, null);
    }

    public static File showFileChooser(String[] extensions) {
        return showFileChooser(null, null, JFileChooser.FILES_ONLY, extensions);
    }

    public static File showFileChooser(String basePath, String[] extensions) {
        return showFileChooser(null, basePath, JFileChooser.FILES_ONLY, extensions);
    }

    public static File showFileChooser(String title, String basePath, String[] extensions) {
        return showFileChooser(title, basePath, JFileChooser.FILES_ONLY, extensions);
    }

    public static File showFileChooser(String title, String basePath, int selectionMode, String[] extensions) {
        JFileChooser chooser = new JFileChooser();
        if (StrUtil.isNotBlank(basePath)) {
            chooser.setCurrentDirectory(new File(basePath));
        }
        if (StrUtil.isBlank(title)) {
            title = "选择文件...";
        }
        chooser.setDialogTitle(title);
        if (extensions != null && extensions.length != 0) {
            FileFilter[] choosableFileFilters = chooser.getChoosableFileFilters();
            for (FileFilter choosableFileFilter : choosableFileFilters) {
                chooser.removeChoosableFileFilter(choosableFileFilter);
            }

            chooser.addChoosableFileFilter(new FileNameExtensionFilter(StrUtil.join(", ", extensions), extensions));
        }
        chooser.setPreferredSize(new Dimension(1100, 750));
        chooser.setFileSelectionMode(selectionMode);
        chooser.showDialog(null, "选择");
        return chooser.getSelectedFile();
    }


    /**
     * 获取剪贴板内容(粘贴)
     */
    public String getClipboardString() {
        //获取系统剪贴板
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //获取剪贴板内容
        Transferable trans = clipboard.getContents(null);
        if(trans != null) {
            //判断剪贴板内容是否支持文本
            if(trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String clipboardStr = null;
                try {
                    //获取剪贴板的文本内容
                    clipboardStr = (String) trans.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return clipboardStr;
            }
        }
        return null;
    }

    /**
     * 设置剪贴板内容(复制)
     */
    public static void setClipboardString(String str) {
        //获取协同剪贴板，单例
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //封装文本内容
        Transferable trans = new StringSelection(str);
        //把文本内容设置到系统剪贴板上
        clipboard.setContents(trans, null);
    }


    public static JDialog showStartingDialog() {
        JDialog startingDialog = new JDialog();
        startingDialog.add(new JLabel(UIUtil.getImageIcon("img/yue.gif")));

        startingDialog.setSize(400, 225);
        startingDialog.setUndecorated(true);
        startingDialog.setResizable(false);

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        int w = startingDialog.getWidth();
        int h = startingDialog.getHeight();
        startingDialog.setLocation((width - w) / 2, (int) ((height - h) / 2 * 0.86));

//        dialog.setLocationRelativeTo(null);
        startingDialog.setVisible(true);

        return startingDialog;
    }


    /**
     * 设置自动滚动到底部
     *
     * @param component  组件
     * @param autoscroll 是否自动滚动
     */
    public static void setMsgBoxAutoscroll(Component component, boolean autoscroll) {
        int updatePolicy = (autoscroll) ? DefaultCaret.ALWAYS_UPDATE : DefaultCaret.NEVER_UPDATE;
        if (component instanceof JTextArea) {
            JTextArea textArea = (JTextArea) component;
            DefaultCaret caret = (DefaultCaret) textArea.getCaret();
            caret.setUpdatePolicy(updatePolicy);
        }
    }


    public static void cleanGap(JPanel panel) {
        LayoutManager layout = panel.getLayout();
        if (layout instanceof FlowLayout) {
            FlowLayout f = (FlowLayout) layout;
            //水平间距
            f.setHgap(0);
            //组件垂直间距
            f.setVgap(0);
        }
    }

    public static <E> ListModel<E> toListModel(List<E> listData) {
        DefaultListModel<E> defaultListModel = new DefaultListModel<>();
        for (E datum : listData) {
            defaultListModel.addElement(datum);
        }
        return defaultListModel;
    }

    public static <E extends JTextComponent> E onlyShowText(E textComponent) {
        textComponent.setBorder(new EmptyBorder(0, 0, 0, 0));
        textComponent.setBackground(new Color(60, 63, 65));
        textComponent.setEditable(false);
        if (textComponent instanceof JTextArea) {
            ((JTextArea) textComponent).setLineWrap(true);
        }
        return textComponent;
    }


    /**
     * 给输入组件增加输入停顿事件，
     * todo 在重置任务时可能要做一些处理，这个方法无法提供，暂时没用
     *
     * @param textComponent      输入组件
     * @param inputPauseListener 输入停顿事件
     * @param delay              停顿时长
     */
    public static void addInputPauseListener(JTextComponent textComponent, InputPauseListener inputPauseListener, long delay) {
        textComponent.getActionMap().put("xxx", new AttachContainer());
        textComponent.getDocument().addDocumentListener(new DocumentListener() {

            private DelayTask task = TaskUtil.newDelayTask(inputPauseListener::inputPaused, delay);

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
                    task.reset();

                    if (TaskUtil.containsTask(task)) {
                        TaskUtil.replaceTask(task);
                    } else {
                        TaskUtil.addTask(task);
                    }
                });
            }
        });
    }


    private static class AttachContainer extends AbstractAction {

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // do noting
        }
    }


//    // 同步设置
//    public static <T> JTextField syncConfig(JTextField textField, SerializableSupplier<T> tl) {
//        String confFieldName = LambdaUtil.getOriginFieldName(tl);
//        Runnable syncConfRunnable = () -> EasyCodeConfig.setValue(confFieldName, textField.getText());
//
//        textField.addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusLost(FocusEvent e) {
//                EasyCodeConfig.setValue(confFieldName, textField.getText());
//            }
//        });
//
//
//
//        textField.getDocument().addDocumentListener(new DocumentListener() {
//            final DelayTask syncConfigTask = TaskUtil.newDelayTask(syncConfRunnable, 300);
//
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                addTask();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                addTask();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                addTask();
//            }
//
//            private void addTask() {
//                ThreadUtil.execAsync(() -> {
//                    // 如果任务已存在，则替换，否则就增加
//                    syncConfigTask.reset();
//                    if (TaskUtil.containsTask(syncConfigTask)) {
//                        TaskUtil.replaceTask(syncConfigTask);
//                    } else {
//                        TaskUtil.addTask(syncConfigTask);
//                    }
//                });
//            }
//        });
//
//        // setText无法触发事件
//        textField.setText(String.valueOf(tl.get()));
//
//        return textField;
//    }

    public static JButton newRefreshButton() {
        JButton refresh = new JButton(UIUtil.getImageIcon("img/r1.png", 20, 20));
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                refresh.setIcon(UIUtil.getImageIcon("img/r2.png", 20, 20));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                refresh.setIcon(UIUtil.getImageIcon("img/r1.png", 20, 20));
            }
        });
        return refresh;
    }


    public static JPanel hwrap(JComponent slaveComponent, JComponent masterComponent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(slaveComponent, BorderLayout.WEST);
        panel.add(masterComponent, BorderLayout.CENTER);
        return panel;
    }


    public static Map<String, Object> form(Container container) {
        Map<String, Object> map = new LinkedHashMap<>();
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (!component.isVisible()) {
                continue;
            }

            String name = component.getName();

            if (component instanceof JLabel) {

            } else if (component instanceof AttachField) {
                if (name == null) {
                    continue;
                }
                Object value = ((AttachField) component).getAttach();
                map.put(name, value);
            } else if (component instanceof JTextField) {
                if (name == null) {
                    continue;
                }
                String value = ((JTextField) component).getText();
                map.put(name, value);
            } else if (component instanceof JTextArea) {
                if (name == null) {
                    continue;
                }
                String value = ((JTextArea) component).getText();
                map.put(name, value);
            } else if (component instanceof JCheckBox) {
                if (name == null) {
                    continue;
                }
                boolean value = ((JCheckBox) component).isSelected();
                map.put(name, value);
            } else if (component instanceof JComboBox) {
                if (name == null) {
                    continue;
                }
                Object value = ((JComboBox<?>) component).getSelectedItem();
                map.put(name, value);
            } else if (component instanceof JToggleButton) {
                if (name == null) {
                    continue;
                }
                boolean value = ((JToggleButton) component).getModel().isSelected();
                map.put(name, value);
            } else if (component instanceof Container) {
                map.putAll(form((Container) component));
                continue;
            }
        }
        return map;
    }


    public static Component removeComponentFromCard(CardLayout cardLayout, String name) {
        Component component = getComponentFromCard(cardLayout, name);
        if (component != null) {
            cardLayout.removeLayoutComponent(component);
        }
        return component;
    }

    public static Component getComponentFromCard(CardLayout cardLayout, String name) {
        // Vector<CardLayout.Card> vector
        Vector vector = (Vector) ReflectUtil.getFieldValue(cardLayout, "vector");
        Enumeration elements = vector.elements();
        while (elements.hasMoreElements()) {
            Object o = elements.nextElement();
            Object cardName = ReflectUtil.getFieldValue(o, "name");
            if (name.equals(cardName)) {
                return (Component) ReflectUtil.getFieldValue(o, "comp");
            }
        }
        return null;
    }


    public static Component getComponentByName(Container container, String name) {
        Component[] components = container.getComponents();
        if (components == null) {
            return null;
        }
        for (Component component : components) {
            if (name.equals(component.getName())) {
                return component;
            }
            if (component instanceof Container) {
                Component find = getComponentByName((Container) component, name);
                if (find != null) {
                    return find;
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {

    }

}
