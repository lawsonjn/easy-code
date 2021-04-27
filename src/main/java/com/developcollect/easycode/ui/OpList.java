package com.developcollect.easycode.ui;

import com.developcollect.easycode.utils.UIUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class OpList<E> extends JPanel {



    private JList<E> jList;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton refreshBtn;

    private MouseListener defaultRemoveBtnMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            removeSelectedItems();
        }
    };

    public OpList() {
        super(new BorderLayout());
        jList = new JList<>(new DefaultListModel<>());
        JScrollPane scrollPane = new JScrollPane(jList);
        scrollPane.setBorder(null);
        addBtn = new JButton("+");
        removeBtn = new JButton("-");
        refreshBtn = new JButton("r");
        Dimension btnSize = new Dimension(22, 22);

        addBtn.setPreferredSize(btnSize);
        addBtn.setMaximumSize(btnSize);
        addBtn.setMinimumSize(btnSize);
        addBtn.setBorder(null);

        removeBtn.setPreferredSize(btnSize);
        removeBtn.setMaximumSize(btnSize);
        removeBtn.setMinimumSize(btnSize);
        removeBtn.setBorder(null);

        refreshBtn.setPreferredSize(btnSize);
        refreshBtn.setMaximumSize(btnSize);
        refreshBtn.setMinimumSize(btnSize);
        refreshBtn.setBorder(null);

        this.add(scrollPane, BorderLayout.CENTER);
        this.add(UIUtil.vb(addBtn, removeBtn, refreshBtn), BorderLayout.EAST);
        this.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));


        fixBackground(addBtn);
        fixBackground(removeBtn);
        fixBackground(refreshBtn);
        fixBackground(this);

        addRemoveBtnMouseListener(defaultRemoveBtnMouseListener);
    }


    @Override
    public Dimension getPreferredSize() {
        if (this.isPreferredSizeSet()) {
            return super.getPreferredSize();
        }
        Dimension listPreferredSize = jList.getPreferredSize();
        Dimension btnPreferredSize = addBtn.getPreferredSize();
        listPreferredSize.width += btnPreferredSize.width;
        return listPreferredSize;
    }

    public void setModel(ListModel<E>  listModel) {
        jList.setModel(listModel);
    }

    public int getSelectedIndex() {
        return jList.getSelectedIndex();
    }

    public int[] getSelectedIndices() {
        return jList.getSelectedIndices();
    }

    public List<E> getAllItem() {
        ListModel<E> model = jList.getModel();
        if (model instanceof DefaultListModel) {
            List<E> list = new ArrayList<>(model.getSize());
            Enumeration<E> elements = ((DefaultListModel<E>) model).elements();
            while (elements.hasMoreElements()) {
                list.add(elements.nextElement());
            }
            return list;
        } else {
            throw new IllegalStateException("不支持的Model:" + model.getClass());
        }
    }

    public E getSelectedItem() {
        int selectedIndex = getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        }
        return getItem(selectedIndex);
    }

    public List<E> getSelectedItems() {
        int[] selectedIndices = getSelectedIndices();
        List<E> items = new ArrayList<>();
        for (int selectedIndex : selectedIndices) {
            items.add(getItem(selectedIndex));
        }
        return items;
    }

    public E getItem(int idx) {
        ListModel<E> model = jList.getModel();
        if (model instanceof DefaultListModel) {
            return ((DefaultListModel<E>) model).get(idx);
        } else {
            throw new IllegalStateException("不支持的Model:" + model.getClass());
        }
    }

    public void addItem(E item) {
        ListModel<E> model = jList.getModel();
        if (model instanceof DefaultListModel) {
            ((DefaultListModel<E>) model).addElement(item);
        } else {
            throw new IllegalStateException("不支持的Model:" + model.getClass());
        }
    }

    public E removeItemAt(int idx) {
        ListModel<E> model = jList.getModel();
        if (model instanceof DefaultListModel) {
            E item = getItem(idx);
            ((DefaultListModel<E>) model).removeElementAt(idx);
            return item;
        } else {
            throw new IllegalStateException("不支持的Model:" + model.getClass());
        }
    }

    public List<E> removeAllItem() {
        ListModel<E> model = jList.getModel();

        if (model instanceof DefaultListModel) {
            List<E> removed = new ArrayList<>(model.getSize());
            for (int i = 0; i < model.getSize(); i++) {
                removed.add(((DefaultListModel<E>) model).get(i));
            }
            ((DefaultListModel<E>) model).removeAllElements();
            return removed;
        } else {
            throw new IllegalStateException("不支持的Model:" + model.getClass());
        }
    }

    public void setSelectedIndex(int idx) {
        jList.setSelectedIndex(idx);
    }

    public void addListSelectionListener(ListSelectionListener listener) {
        jList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        jList.removeListSelectionListener(listener);
    }

    public void addAddBtnMouseListener(MouseListener mouseListener) {
        addBtn.addMouseListener(mouseListener);
    }

    public void addRemoveBtnMouseListener(MouseListener mouseListener) {
        MouseListener[] mouseListeners = refreshBtn.getMouseListeners();
        for (MouseListener listener : mouseListeners) {
            if (listener == defaultRemoveBtnMouseListener) {
                removeBtn.removeMouseListener(defaultRemoveBtnMouseListener);
                break;
            }
        }
        removeBtn.addMouseListener(mouseListener);
    }

    public void addRefreshBtnMouseListener(MouseListener mouseListener) {
        refreshBtn.addMouseListener(mouseListener);
    }

    public void addListMouseListener(MouseListener mouseListener) {
        jList.addMouseListener(mouseListener);
    }



    public List<E> removeSelectedItems() {
        int[] selectedIndices = jList.getSelectedIndices();
        List<E> removed = new ArrayList<>(selectedIndices.length);
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            removed.add(removeItemAt(selectedIndices[i]));
        }
        return removed;
    }

    public List<E> clear() {
        return removeAllItem();
    }


    private static void fixBackground(Component component) {
        component.setBackground(new Color(69,73,74));
    }
}
