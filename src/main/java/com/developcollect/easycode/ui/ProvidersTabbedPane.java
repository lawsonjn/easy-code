package com.developcollect.easycode.ui;

import com.developcollect.easycode.Provider;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/27 10:42
 */
public class ProvidersTabbedPane extends JTabbedPane {

    private JFrame jFrame;
    private List<Provider> providers = new ArrayList<>();

    public ProvidersTabbedPane(JFrame jFrame) {
        this.jFrame = jFrame;
    }

    public void addProvider(Provider provider) {
        this.addTab(provider.getTitle(), provider.getComponent(jFrame));
        providers.add(provider);
    }

    public Provider getSelectedProvider() {
        return providers.get(getSelectedIndex());
    }
}
