package com.developcollect.easycode.provider.fakedata;

import com.developcollect.easycode.core.fakedata.IDbDataFaker;

import javax.swing.*;
import java.util.Map;

public interface FakerProvider<E extends IDbDataFaker> {
    String name();
    E faker(Map<String, Object> map);
    JComponent component(E dbDataFaker);
}
