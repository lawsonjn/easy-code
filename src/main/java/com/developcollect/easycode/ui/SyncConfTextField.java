package com.developcollect.easycode.ui;

import com.developcollect.core.lang.annotation.SerializableSupplier;
import com.developcollect.core.task.DelayTask;
import com.developcollect.core.task.TaskUtil;
import com.developcollect.core.thread.ThreadUtil;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.easycode.EasyCodeConfig;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/13 14:43
 */
public class SyncConfTextField extends IconTextField {

    private String confFieldName;

    public <T> SyncConfTextField(SerializableSupplier<T> tl) {
        this.confFieldName = LambdaUtil.getFieldName(tl);
        this.setText(String.valueOf(tl.get()));
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                EasyCodeConfig.setValue(confFieldName, SyncConfTextField.this.getText());
            }
        });

        this.getDocument().addDocumentListener(new DocumentListener() {
            private DelayTask task = TaskUtil.newDelayTask(() -> EasyCodeConfig.setValue(confFieldName, SyncConfTextField.this.getText()), 300);

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
                    // 如果任务已存在，则替换，否则就增加
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

    @Override
    public void setText(String t) {
        super.setText(t);
        EasyCodeConfig.setValue(confFieldName, t);
    }


}
