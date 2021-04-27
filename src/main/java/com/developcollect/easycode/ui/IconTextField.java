package com.developcollect.easycode.ui;

import cn.hutool.core.img.FontUtil;
import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/30 11:40
 */
public class IconTextField extends HintTextField {

    public static final int PLACE_LEFT = 1;
    public static final int PLACE_RIGHT = 2;

    private ImageIcon icon;
    private ImageIcon printIcon;
    private Rectangle iconRectangle = new Rectangle();
    private int place = PLACE_LEFT;
    private Insets originMargin;
    private Insets originInsets;
    private List<MouseListener> iconMouseListeners = new ArrayList<>();
    private List<MouseMotionListener> iconMouseMotionListener = new ArrayList<>();

    {
        this.setFont(FontUtil.createSansSerifFont(15));
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (iconRectangle.contains(e.getX(), e.getY())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    for (MouseListener iconMouseListener : iconMouseListeners) {
                        iconMouseListener.mouseEntered(e);
                    }
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    for (MouseListener iconMouseListener : iconMouseListeners) {
                        iconMouseListener.mouseExited(e);
                    }
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (iconRectangle.contains(e.getX(), e.getY())) {
                    for (MouseListener iconMouseListener : iconMouseListeners) {
                        iconMouseListener.mouseClicked(e);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (iconRectangle.contains(e.getX(), e.getY())) {
                    for (MouseListener iconMouseListener : iconMouseListeners) {
                        iconMouseListener.mouseClicked(e);
                    }
                }
            }
        });
    }


    public IconTextField() {
    }

    public IconTextField(String text) {
        super(text);
    }

    public IconTextField(String text, ImageIcon icon) {
        super(text);
        setIcon(icon);
    }

    public IconTextField(int columns) {
        super(columns);
    }

    public IconTextField(String text, int columns) {
        super(text, columns);
    }

    public IconTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
    }

    public void setIcon(ImageIcon icon) {
        this.icon = icon;
        if (this.printIcon != null) {
            this.printIcon = null;
            // 触发重绘
            this.repaint();
        }
    }

    public void setPlace(int place) {
        this.place = place;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (icon == null) {
            super.paintComponent(g);
        } else {
            int height = this.getHeight();
            int width = this.getWidth();
            Insets insets = getInsets();

            int iconHeight = height - insets.top - insets.bottom - 4;
            int iconWidth = (int) (icon.getIconWidth() * ((double) iconHeight / icon.getIconHeight()));

            if (iconHeight <= 0 || iconWidth <= 0 || iconWidth > width || iconHeight > height) {
                super.paintComponent(g);
                return;
            }

            if (printIcon == null || printIcon.getIconWidth() != iconWidth || printIcon.getIconHeight() != iconHeight) {
                printIcon = UIUtil.resizeIcon(icon, iconWidth, iconHeight);
                if (originMargin == null) {
                    originMargin = getMargin();
                }
                if (originInsets == null) {
                    originInsets = insets;
                }
                if (place == PLACE_RIGHT) {
                    this.setMargin(new Insets(originMargin.top, originMargin.left, originMargin.bottom, originMargin.right + iconWidth + 3));
                } else {
                    this.setMargin(new Insets(originMargin.top, originMargin.left + iconWidth + 3, originMargin.bottom, originMargin.right));
                }
            }

            // 当图标在右边，并且输入框被拉长时需要重新定位
            if (place == PLACE_RIGHT) {
                this.iconRectangle.setBounds(width - iconWidth - originInsets.right, (height - iconHeight) / 2, iconWidth, iconHeight);
            } else {
                this.iconRectangle.setBounds(originInsets.left, (height - iconHeight) / 2, iconWidth, iconHeight);
            }

            super.paintComponent(g);
            //在文本框中画上之前图片
            printIcon.paintIcon(this, g, this.iconRectangle.x, this.iconRectangle.y);
        }

    }


    public void addIconMouseListener(MouseListener mouseListener) {
        this.iconMouseListeners.add(mouseListener);
    }

    public void removeIconMouseListener(MouseListener mouseListener) {
        this.iconMouseListeners.remove(mouseListener);
    }

    public void addIconMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.iconMouseMotionListener.add(mouseMotionListener);
    }


    public void removeIconMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.iconMouseMotionListener.add(mouseMotionListener);
    }
}
