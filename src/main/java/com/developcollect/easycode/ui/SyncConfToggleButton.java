package com.developcollect.easycode.ui;

import com.developcollect.core.lang.annotation.SerializableSupplier;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.easycode.EasyCodeConfig;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/21 17:22
 */
public class SyncConfToggleButton extends SwitchButton {

    private String confFieldName;

    private final ItemListener syncConfListener = e -> {
        if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
            EasyCodeConfig.setValue(confFieldName, SyncConfToggleButton.this.isSelected());
        }
    };

    public SyncConfToggleButton(SerializableSupplier<Boolean> supplier) {
        super();
        this.confFieldName = LambdaUtil.getFieldName(supplier);
        this.setSelected(supplier.get());
        this.addItemListener(syncConfListener);
    }
}
