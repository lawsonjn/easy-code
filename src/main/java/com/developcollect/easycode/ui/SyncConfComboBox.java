package com.developcollect.easycode.ui;

import com.developcollect.core.lang.annotation.SerializableSupplier;
import com.developcollect.core.utils.LambdaUtil;
import com.developcollect.easycode.EasyCodeConfig;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author Zhu Kaixiao
 * @version 1.0
 * @date 2020/10/13 15:34
 */
public class SyncConfComboBox extends JComboBox {
    private String confFieldName;
    private Map<String, Item> itemMap = new HashMap<>();

    private final ItemListener syncConfListener = e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            EasyCodeConfig.setValue(confFieldName, e.getItem().toString());
        }
    };

    public <T> SyncConfComboBox(SerializableSupplier<T> tl) {
        this.confFieldName = LambdaUtil.getFieldName(tl);
        this.addItem(tl.get());
        this.setSelectedIndex(0);
        this.addItemListener(syncConfListener);
    }

    public <T> SyncConfComboBox(List<Item> items, SerializableSupplier<T> tl) {
        super(items.stream().map(Item::getName).toArray());
        this.confFieldName = LambdaUtil.getFieldName(tl);
        this.itemMap = items.stream().collect(Collectors.toMap(Item::getName, t -> t));
        this.setSelectedItem(tl.get());
        this.addItemListener(syncConfListener);
    }

    public <T> SyncConfComboBox(Object[] items, SerializableSupplier<T> tl) {
        this(Arrays.stream(items).map(i -> new Item() {
            @Override
            public String getName() {
                return i.toString();
            }

            @Override
            public Object getData() {
                return i;
            }
        }).collect(Collectors.toList()), tl);
    }

    public Item getItem(Object str) {
        return itemMap.get(str);
    }

    public Item getItem() {
        return itemMap.get(this.getSelectedItem());
    }


    @Override
    public void removeAllItems() {
        itemMap.clear();
        super.removeAllItems();
    }

    @Override
    public void addItem(Object item) {
        if (item instanceof Item) {
            Item ii = (Item) item;
            itemMap.put(ii.getName(), ii);
            super.addItem(ii.getName());
        } else {
            super.addItem(item);
        }
    }

    public interface Item {

        String getName();

        <T> T getData();
    }
}
