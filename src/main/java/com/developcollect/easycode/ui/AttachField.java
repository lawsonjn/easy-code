package com.developcollect.easycode.ui;

import javax.swing.*;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/14 10:59
 */
public class AttachField extends JTextField {

    private Object attach;

    {
        this.setEditable(false);
    }

    public Object getAttach() {
        return attach;
    }

    public void setAttach(Object attach) {
        this.attach = attach;
        if (attach == null) {
            this.setText("");
        } else {
            this.setText(attach.toString());
        }
    }
}
