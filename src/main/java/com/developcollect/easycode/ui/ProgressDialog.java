package com.developcollect.easycode.ui;

import cn.hutool.core.thread.ThreadUtil;
import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;


public class ProgressDialog {

    private Window parent;
    private JDialog dialog;
    private JProgressBar progressBar;
    private JLabel lbStatus;
    private JButton btnCancel;
    private Consumer<ProgressDialog> callable;
    private Runnable completeRunnable;

    private ProgressDialog(Window parent, Consumer<ProgressDialog> callable) {
        this.parent = parent;
        this.callable = callable;
        initUI();
        startThread();
        dialog.setVisible(true);
    }

    public static void show(Window parent, Consumer<ProgressDialog> callable) {
        new ProgressDialog(parent, callable);
    }

    public static void main(String[] args) throws Exception {


        Consumer<ProgressDialog> booleanCallable = progressDialog -> {
            progressDialog.getProgressBar().setMaximum(5);

            for (int i = 1; i <= 5; i++) {
                ThreadUtil.sleep(1000);
                System.out.println(i);
                progressDialog.getProgressBar().setValue(i);
            }

            progressDialog.setCompleteRunnable(() -> {
                UIUtil.showInfo("执行完成");
            });

        };


        ProgressDialog.show(null, booleanCallable);

    }

    private void initUI() {
        if (parent instanceof Dialog) {
            dialog = new JDialog((Dialog) parent, true);
        } else if (parent instanceof Frame) {
            dialog = new JDialog((Frame) parent, true);
        } else {
            dialog = new JDialog((Frame) null, true);
        }

        final JPanel mainPane = new JPanel(null);
        progressBar = new JProgressBar();
        lbStatus = new JLabel("请等待...");
        btnCancel = new JButton("Cancel");
//        btnCancel.addActionListener(this);

        mainPane.add(progressBar);
        mainPane.add(lbStatus);
        //mainPane.add(btnCancel);

        dialog.getContentPane().add(mainPane);
        // 除去title
        dialog.setUndecorated(true);
        dialog.setResizable(true);
        dialog.setSize(390, 100);
        //设置此窗口相对于指定组件的位置
        dialog.setLocationRelativeTo(parent);
        // 不允许关闭
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);


        mainPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layout(mainPane.getWidth(), mainPane.getHeight());
            }
        });
    }

    private void startThread() {
        ThreadUtil.execAsync(() -> {
            try {
                callable.accept(ProgressDialog.this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 关闭进度提示框
            dialog.dispose();

            if (this.completeRunnable != null) {
                this.completeRunnable.run();
            }
        });
    }

    private void layout(int width, int height) {
        progressBar.setBounds(20, 20, 350, 15);
        lbStatus.setBounds(20, 50, 350, 25);
        btnCancel.setBounds(width - 85, height - 31, 75, 21);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getLbStatus() {
        return lbStatus;
    }

    public void setCompleteRunnable(Runnable completeRunnable) {
        this.completeRunnable = completeRunnable;
    }
}