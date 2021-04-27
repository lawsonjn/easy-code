package com.developcollect.easycode.ui;

import com.formdev.flatlaf.FlatDarculaLaf;
import org.junit.Test;

import javax.swing.*;

import java.awt.*;

public class OpListTest {

    @Test
    public void tt() {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarculaLaf.install();
            JFrame frame = new JFrame();
            frame.add(new OpList<>());
            frame.setSize(new Dimension(400, 600));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });

    }
}