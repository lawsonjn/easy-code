package com.developcollect.easycode.ui;



import com.developcollect.core.utils.StrUtil;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/25 11:34
 */
public class HintTextField extends JTextField implements FocusListener {

    private String hintText;
    private boolean isHint = false;
    private Color prevForeground;

    {
        this.addFocusListener(this);
    }

    public HintTextField() {
    }

    public HintTextField(String text) {
        super(text);
    }

    public HintTextField(int columns) {
        super(columns);
    }

    public HintTextField(String text, int columns) {
        super(text, columns);
    }

    public HintTextField(Document doc, String text, int columns) {
        super(doc, text, columns);
    }


    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
        this.focusLost(null);
    }


    @Override
    public void setText(String t) {
        doSetText(t);
        isHint = false;
    }

    private void doSetText(String t) {
        super.setText(t);
    }

    @Override
    public void focusGained(FocusEvent e) {
        //获取焦点时，清空提示内容
        String temp = this.getText();
        if (this.isHint && temp.equals(getHintText())) {
            this.isHint = false;
            this.doSetText("");
            this.setForeground(this.prevForeground);
        }

    }

    @Override
    public void focusLost(FocusEvent e) {
        //失去焦点时，没有输入内容，显示提示内容
        String temp = this.getText();
        if (!this.isHint && StrUtil.isNotBlank(getHintText()) && "".equals(temp)) {
            this.isHint = true;
            this.prevForeground = this.getForeground();
            this.setForeground(Color.GRAY);
            this.doSetText(getHintText());
        }
    }


    public boolean isHint() {
        return isHint;
    }

}
