package com.developcollect.easycode.ui;

import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;


/**
 * 弹窗
 * 分成上下两部分
 * 上面的部分是自定义的组件
 * 下面是按钮
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/9 9:47
 */
public class SDialog extends JDialog {

    private int value = -1;

    private SDialog() {
    }


    public int display() {
        this.setVisible(true);
        return value;
    }



    public static SDialogBuilder builder() {
        return new SDialogBuilder();
    }


    public static void main(String[] args) {
        // 显示
        int xxdd = builder()
                .title("xxdd")
                .body(addDbInfoPanel())
                .foot("连接测试", "-", "经复", "-", "确定", "取消")
                .resizable(false)
                .build()
                .display();

        System.out.println("@######");

        System.out.println(xxdd);
    }


    /**
     * 弹窗构造器
     * //  this.setExtendedState(JFrame.MAXIMIZED_BOTH); //最大化
     * //    this.setAlwaysOnTop(true);    //总在最前面
     * //    this.setResizable(false);    //不能改变大小
     * //    this.setUndecorated(true);    //不要边框
     */
    public static class SDialogBuilder {
        private String title;
        private Component body;
        private Component foot;
        private Component parent;
        private boolean modal = true;
        private String[] defaultFootBtnTexts = null;
        private Border border = BorderFactory.createEmptyBorder(10,10,10,10);
        private Image iconImage;
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
        private boolean resizable = true;
        private boolean alwaysOnTop = false;
        /**
         * 是否不要边框
         */
        private boolean undecorated = false;

        private List<WindowListener> windowListeners = new ArrayList<>();

        private List<FootMouseListener> footMouseListeners = new ArrayList<>();


        public SDialogBuilder() {
        }

        public SDialogBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SDialogBuilder body(Component body) {
            this.body = body;
            return this;
        }

        public SDialogBuilder foot(Component foot) {
            this.foot = foot;
            return this;
        }

        public SDialogBuilder parent(Component parent) {
            this.parent = parent;
            return this;
        }

        public SDialogBuilder modal(boolean modal) {
            this.modal = modal;
            return this;
        }

        public SDialogBuilder foot() {
            return foot("-", "取消", "确定");
        }

        public SDialogBuilder foot(String... footBtnTexts) {
            defaultFootBtnTexts = footBtnTexts;
            return this;
        }

        public SDialogBuilder border(Border border) {
            this.border = border;
            return this;
        }

        public SDialogBuilder icon(Image iconImage) {
            this.iconImage = iconImage;
            return this;
        }

        public SDialogBuilder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public SDialogBuilder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public SDialogBuilder location(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public SDialogBuilder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public SDialogBuilder alwaysOnTop(boolean alwaysOnTop) {
            this.alwaysOnTop = alwaysOnTop;
            return this;
        }

        public SDialogBuilder undecorated(boolean undecorated) {
            this.undecorated = undecorated;
            return this;
        }

        public SDialogBuilder windowListener(WindowListener windowListener) {
            this.windowListeners.add(windowListener);
            return this;
        }

        public SDialogBuilder footMouseListener(FootMouseListener footMouseListener) {
            this.footMouseListeners.add(footMouseListener);
            return this;
        }

        public SDialog build() {
            SDialog dialog = new SDialog();
            Container container = dialog.getContentPane();
            container.setLayout(new BorderLayout());
            body = this.body == null ? new JPanel() : this.body;
            container.add(body, BorderLayout.CENTER);



            if (border != null) {
                JComponent jc = (JComponent) container;
                jc.setBorder(border);
            }


            if (foot != null) {
                container.add(foot, BorderLayout.SOUTH);
            } else if (defaultFootBtnTexts != null) {
                Box footBox = Box.createHorizontalBox();
                foot = footBox;

                for (int i = 0; i < defaultFootBtnTexts.length; i++) {
                    String defaultFootBtnText = defaultFootBtnTexts[i];

                    if ("-".equals(defaultFootBtnText)) {
                        footBox.add(Box.createHorizontalGlue());
                    } else {
                        MouseAdapter footMouseAdapter = createFootMouseAdapter(dialog, i);

                        JButton button = new JButton(defaultFootBtnText);
                        button.setName("footBtn_" + i);
                        button.addMouseListener(footMouseAdapter);
                        button.addMouseMotionListener(footMouseAdapter);
                        button.addMouseWheelListener(footMouseAdapter);

                        footBox.add(button);
                        if (i + 1 < defaultFootBtnTexts.length) {
                            if (!"-".equals(defaultFootBtnTexts[i + 1])) {
                                footBox.add(Box.createHorizontalStrut(10));
                            }
                        }
                    }
                }
                container.add(foot, BorderLayout.SOUTH);
            }

            if (parent != null) {
                dialog.setLocationRelativeTo(parent);

                if (parent instanceof Window) {
                    List<Image> images = ((Window) parent).getIconImages();
                    if (images != null && !images.isEmpty()) {
                        dialog.setIconImage(images.get(0));
                    }
                }
            }




            if (title != null) {
                dialog.setTitle(title);
            }

            if (iconImage != null) {
                dialog.setIconImage(iconImage);
            }



            // 位置和长宽
            if (width == null || height == null) {
                boolean isPreferredSizeSet = body.isPreferredSizeSet();
                Dimension preferredSize = body.getPreferredSize();
                width = preferredSize.width;
                height = preferredSize.height;
                if (foot != null) {
                    height += foot.getPreferredSize().height;
                }
                if (!isPreferredSizeSet) {
                    width = Math.max(width, 400);
                    height = Math.max(height, 450);
                }
            }

            if (x == null || y == null) {
                if (parent != null) {
                    Rectangle bounds = parent.getBounds();
                    x = (int) ((bounds.getWidth() - width) / 2 + bounds.x);
                    y = (int) ((bounds.getHeight() - height) / 2 + bounds.y);
                } else {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    x = (int) ((screenSize.getWidth() - width) / 2);
                    y = (int) ((screenSize.getHeight() - height) / 2);
                }
            }
            dialog.setBounds(x, y, width, height);

            for (WindowListener windowListener : windowListeners) {
                dialog.addWindowListener(windowListener);
            }



            // 是否为模态框
            dialog.setModal(modal);
            // 是否能改变大小
            dialog.setResizable(resizable);
            // 是否总在最前面
            dialog.setAlwaysOnTop(alwaysOnTop);
            // 是否不要边框
            dialog.setUndecorated(undecorated);

            return dialog;
        }


        private MouseAdapter createFootMouseAdapter(SDialog dialog, int idx) {

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (footMouseListeners.isEmpty()) {
                        dialog.value = idx;
                        dialog.dispose();
                    } else {
                        for (FootMouseListener footMouseListener : footMouseListeners) {
                            footMouseListener.mouseClicked(new FootMouseEvent(dialog, e, idx));
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mousePressed(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseReleased(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseEntered(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseExited(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseWheelMoved(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseDragged(new FootMouseEvent(dialog, e, idx));
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    for (FootMouseListener footMouseListener : footMouseListeners) {
                        footMouseListener.mouseMoved(new FootMouseEvent(dialog, e, idx));
                    }
                }
            };

            return mouseAdapter;
        }
    }



    public interface FootMouseListener {

        default void mouseClicked(FootMouseEvent footMouseEvent) {}

        default void mousePressed(FootMouseEvent footMouseEvent) {}

        default void mouseReleased(FootMouseEvent footMouseEvent) {}

        default void mouseEntered(FootMouseEvent footMouseEvent) {}

        default void mouseExited(FootMouseEvent footMouseEvent) {}

        default void mouseWheelMoved(FootMouseEvent footMouseEvent){}

        default void mouseDragged(FootMouseEvent footMouseEvent){}

        default void mouseMoved(FootMouseEvent footMouseEvent){}
    }



    public static class FootMouseEvent extends EventObject {
        private MouseEvent mouseEvent;
        private int footIdx;


        /**
         * Constructs a prototypical Event.
         *
         * @param source The object on which the Event initially occurred.
         * @throws IllegalArgumentException if source is null.
         */
        public FootMouseEvent(Object source, MouseEvent mouseEvent, int footIdx) {
            super(source);
            this.mouseEvent = mouseEvent;
            this.footIdx = footIdx;
        }

        public int getFootIdx() {
            return footIdx;
        }

        public MouseEvent getMouseEvent() {
            return mouseEvent;
        }


        @Override
        public SDialog getSource() {
            return (SDialog) super.getSource();
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////


    private static JComponent addDbInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel dbUrl = new JLabel("URL：", SwingConstants.RIGHT);
        HintTextField dbUrlIn = new HintTextField();
        dbUrlIn.setHintText("jdbc:mysql://192.168.0.79:3306/db-name");

        JLabel dbUsername = new JLabel("　用户名：", SwingConstants.RIGHT);
        HintTextField dbUsernameIn = new HintTextField();
        dbUsernameIn.setHintText("root");

        JLabel dbPassword = new JLabel("　密码：", SwingConstants.RIGHT);
        HintTextField dbPasswordIn = new HintTextField();
        dbPasswordIn.setHintText("123456");

        JButton saveBtn = new JButton("确定");
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });

        JButton testBtn = new JButton("连接测试");

        UIUtil.bag(panel, dbUrl,          1, 1, 0, 0);
        UIUtil.bag(panel, dbUrlIn,        8, 1, 1, 0);
        UIUtil.bag(panel, Box.createHorizontalGlue(), 0, 1, 0, 0);
        UIUtil.bag(panel, dbUsername,     1, 1, 0, 0);
        UIUtil.bag(panel, dbUsernameIn,   8, 1, 1, 0);
        UIUtil.bag(panel, Box.createHorizontalGlue(), 0, 1, 0, 0);
        UIUtil.bag(panel, dbPassword,     1, 1, 0, 0);
        UIUtil.bag(panel, dbPasswordIn,   8, 1, 1, 0);
        UIUtil.bag(panel, Box.createHorizontalGlue(), 0, 1, 0, 0);
        UIUtil.bag(panel, testBtn,   1, 1, 0, 0);
        UIUtil.bag(panel, saveBtn,   8, 1, 1, 0);

        panel.setPreferredSize(new Dimension(750, 450));
        return panel;
    }


}


