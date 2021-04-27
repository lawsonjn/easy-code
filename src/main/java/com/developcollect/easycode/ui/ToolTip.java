package com.developcollect.easycode.ui;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import javax.swing.*;
import java.awt.*;


@Data
public class ToolTip {

    /**
     * 气泡提示宽
     */
    private int width = 300;

    /**
     * 气泡提示高
     */
    private int height = 100;

    /**
     * 设定循环的步长
     */
    private int step = 30;

    /**
     * 每步时间
     */
    private int stepTime = 30;

    /**
     * 显示时间
     */
    private int displayTime = 6000;

    /**
     * 目前申请的气泡提示数量
     */
    private int countOfToolTip = 0;

    /**
     * 当前最大气泡数
     */
    private int maxToolTip = 0;

    /**
     * 在屏幕上显示的最大气泡提示数量
     */
    private int maxToolTipSceen;

    /**
     * 字体
     */
    private Font font;

    /**
     * 边框颜色
     */
    private Color bgColor;

    /**
     * 背景颜色
     */
    private Color border;

    /**
     * 消息颜色
     */
    private Color messageColor;

    /**
     * 是否要求至顶
     */
    boolean useTop;

    /**
     * 构造函数，初始化默认气泡提示设置
     */
    public ToolTip() {
        // 设定字体
        font = new Font("微软雅黑", 0, 13);
        // 设定边框颜色
        bgColor = new Color(60, 63, 65);
        border = Color.BLACK;
        messageColor = new Color(196, 200, 186);
        useTop = true;
        // 通过调用方法，强制获知是否支持自动窗体置顶
//        try {
//            JWindow.class.getMethod("setAlwaysOnTop",
//                    new Class[] { Boolean.class });
//        } catch (Exception e) {
//            useTop = false;
//        }

    }

    /**
     * 重构JWindow用于显示单一气泡提示框
     */
    @Data
    class ToolTipSingle extends JWindow {
        private static final long serialVersionUID = 1L;

        private JLabel iconLabel = new JLabel();

        private JTextArea message = new JTextArea();

        public ToolTipSingle() {
            initComponents();
        }

        private void initComponents() {
            setSize(ToolTip.this.getWidth(), ToolTip.this.getHeight());

            message.setFont(ToolTip.this.getFont());
            message.setBackground(bgColor);
            message.setMargin(new Insets(4, 4, 4, 4));
            message.setLineWrap(true);
//            message.setWrapStyleWord(true);
            message.setForeground(ToolTip.this.getMessageColor());

            JPanel innerPanel = new JPanel(new BorderLayout(1, 1));
            innerPanel.setBackground(bgColor);
            // 设定外部面板内容边框为风化效果
            innerPanel.setBorder(BorderFactory.createEtchedBorder());
            innerPanel.add(iconLabel, BorderLayout.WEST);
            innerPanel.add(message, BorderLayout.CENTER);
            getContentPane().add(innerPanel);
        }

        /**
         * 动画开始
         */
        public void animate() {
            new Animation(this).start();
        }

    }

    /**
     * 此类处则动画处理
     */
    class Animation extends Thread {

        ToolTipSingle single;

        public Animation(ToolTipSingle single) {
            this.single = single;
        }

        /**
         * 调用动画效果，移动窗体坐标
         *
         * @param posx
         * @param startY
         * @param endY
         * @throws InterruptedException
         */
        private void animateVertically(int posx, int startY, int endY)
                throws InterruptedException {
            single.setLocation(posx, startY);
            if (endY < startY) {
                for (int i = startY; i > endY; i -= step) {
                    single.setLocation(posx, i);
                    Thread.sleep(stepTime);
                }
            } else {
                for (int i = startY; i < endY; i += step) {
                    single.setLocation(posx, i);
                    Thread.sleep(stepTime);
                }
            }
            single.setLocation(posx, endY);
        }

        /**
         * 开始动画处理
         */
        @Override
        public void run() {
            try {
                boolean animate = true;
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle screenRect = ge.getMaximumWindowBounds();
                int screenHeight = screenRect.height;
                int startYPosition;
                int stopYPosition;
                if (screenRect.y > 0) {
                    animate = false;
                }
                maxToolTipSceen = screenHeight / height;
                int posx = screenRect.width - width - 1;
                single.setLocation(posx, screenHeight);
                single.setVisible(true);
                if (useTop) {
                    single.setAlwaysOnTop(true);
                }
                if (animate) {
                    startYPosition = screenHeight;
                    stopYPosition = startYPosition - height - 1;
                    if (countOfToolTip > 0) {
                        stopYPosition = stopYPosition
                                - (maxToolTip % maxToolTipSceen * height);
                    } else {
                        maxToolTip = 0;
                    }
                } else {
                    startYPosition = screenRect.y - height;
                    stopYPosition = screenRect.y;

                    if (countOfToolTip > 0) {
                        stopYPosition = stopYPosition
                                + (maxToolTip % maxToolTipSceen * height);
                    } else {
                        maxToolTip = 0;
                    }
                }

                countOfToolTip++;
                maxToolTip++;

                animateVertically(posx, startYPosition, stopYPosition);
                Thread.sleep(displayTime);
                animateVertically(posx, stopYPosition, startYPosition);

                countOfToolTip--;
                single.setVisible(false);
                single.dispose();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 设定显示的图片及信息
     *
     * @param icon
     * @param msg
     */
    public void setToolTip(Icon icon, String msg, Object... objs) {
//        System.out.println(countOfToolTip);
//        try {
//            // 多个文件变化时,每个间隔2秒
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        ToolTipSingle single = new ToolTipSingle();
        if (icon != null) {
            single.iconLabel.setIcon(icon);
        }
        single.message.setText(StrUtil.format(msg, objs));
        single.animate();
    }


    private static final ToolTip tip = new ToolTip();

    public static void showTip(Icon icon, String msg, Object... objs) {
        tip.setToolTip(icon, msg, objs);
    }


    public static void showTip(String msg, Object... objs) {
        showTip(null, msg, objs);
    }



}