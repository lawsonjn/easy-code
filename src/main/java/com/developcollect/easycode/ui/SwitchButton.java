package com.developcollect.easycode.ui;

import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/23 17:10
 */
public class SwitchButton extends JToggleButton {

    public SwitchButton() {
        this.setSelected(false);
        this.setBorderPainted(false);
        this.setFocusPainted(false);
        // 设置 选中(开) 和 未选中(关) 时显示的图片
        this.setIcon(UIUtil.getImageIcon("img/s1.png", 31, 20));
        this.setSelectedIcon(UIUtil.getImageIcon("img/s2.png", 31, 20));
    }
}
