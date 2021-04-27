package com.developcollect.easycode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public interface Provider extends Comparable<Provider> {

    default int order() {return Integer.MAX_VALUE;}

    default boolean enable() {return true;}

    String getTitle();

    Component getComponent(JFrame frame);



    default void create(ActionEvent e) {}
    default void open(ActionEvent e) {}
    default void save(ActionEvent e) {}
    default void saveNew(ActionEvent e) {}




    @Override
    default int compareTo(Provider o) {
        return Integer.compare(this.order(), o.order());
    }



}
