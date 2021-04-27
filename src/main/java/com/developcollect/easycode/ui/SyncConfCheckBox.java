package com.developcollect.easycode.ui;

import com.developcollect.core.lang.annotation.SerializableSupplier;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.easycode.EasyCodeConfig;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/13 15:43
 */
public class SyncConfCheckBox extends JCheckBox {
    private String confFieldName;
    private final ChangeListener syncConfListener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
            JCheckBox checkBox = (JCheckBox) e.getSource();
            EasyCodeConfig.setValue(confFieldName, checkBox.isSelected());
        }
    };



    public SyncConfCheckBox(SerializableSupplier<Boolean> tl) {
        this.confFieldName = LambdaUtil.getFieldName(tl);
        this.setSelected(tl.get());
        this.addChangeListener(syncConfListener);
    }

    public SyncConfCheckBox(String title, SerializableSupplier<Boolean> tl) {
        super(title);
        this.confFieldName = LambdaUtil.getFieldName(tl);
        this.setSelected(tl.get());
        this.addChangeListener(syncConfListener);
    }
}
