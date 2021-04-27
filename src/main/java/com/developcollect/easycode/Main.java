package com.developcollect.easycode;

import cn.hutool.core.thread.ThreadUtil;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.developcollect.easycode.ui.MainFrame;
import com.developcollect.easycode.ui.UpdateDialog;
import com.developcollect.easycode.utils.UIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/12 13:54
 */
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) {
        if (args.length == 2 && "update".equals(args[0])) {
            // 启动更新面板
            SwingUtilities.invokeLater(() -> {
                FlatDarculaLaf.install();
                new UpdateDialog(args[1]);
            });

        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    FlatDarculaLaf.install();
                    JDialog startingDialog = UIUtil.showStartingDialog();
                    ThreadUtil.execAsync(() -> {
                        try {
                            new MainFrame(startingDialog);
                        } catch (Throwable t) {
                            log.error("启动失败", t);
                            UIUtil.showWaitError("启动失败");
                            startingDialog.dispose();
                            System.exit(1);
                        }
                    });
                } catch (Throwable t) {
                    log.error("启动失败", t);
                    UIUtil.showWaitError("启动失败");
                    System.exit(1);
                }
            });
        }
    }


}
