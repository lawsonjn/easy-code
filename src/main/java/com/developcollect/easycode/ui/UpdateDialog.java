package com.developcollect.easycode.ui;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.developcollect.easycode.utils.UIUtil;
import com.developcollect.easycode.utils.UpdateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 15:33
 */
public class UpdateDialog {

    private static final Logger log = LoggerFactory.getLogger(UpdateDialog.class);

    public UpdateDialog(String originJarPath) {
        ProgressDialog.show(null, progressDialog -> {
            progressDialog.getProgressBar().setIndeterminate(true);
            progressDialog.getLbStatus().setText("正在更新...");
            File file = UpdateUtil.downloadUpdate();

            if (file != null) {
                for (int i = 0; i < 10; i++) {
                    try {
                        FileUtil.copy(FileUtil.getAbsolutePath(file), originJarPath, true);
                        break;
                    } catch (Exception ignore) {
                    }
                    ThreadUtil.sleep(1000);
                }
                progressDialog.setCompleteRunnable(() -> {
                    String[] options = {"确定", "立即重启"};
                    int i = JOptionPane.showOptionDialog(null, "更新完成", "提示",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
                    if (i == 1) {
                        try {
                            RuntimeUtil.exec("javaw -Dfile.encoding=utf-8 -jar " + originJarPath);
                        } catch (Exception e) {
                            log.error("启动EasyCode失败", e);
                            UIUtil.showWaitError("启动EasyCode失败");
                        }
                    }
                    System.exit(1);
                });
            } else {
                progressDialog.setCompleteRunnable(() -> {
                    UIUtil.showWaitError("更新失败！");
                    System.exit(1);
                });
            }
        });
    }



}
