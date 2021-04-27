package com.developcollect.easycode.core.fakedata;



import lombok.Getter;

import java.util.*;

/**
 * 假数据生成上下文
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/14 14:43
 */

public class FakeDataContext {

    private static final String SEP = "!枪出如龙!";

    @Getter
    private long currNo;
    @Getter
    private long total;


    private Map<String, Map<String, Object>> contextMap = new LinkedHashMap<>();
    private Set<String> existsSet = new HashSet<>();

    public FakeDataContext(long total) {
        this.currNo = 0;
        this.total = total;
    }


    public void addFieldValue(String table, String field, Object value) {
        getTableMap(table).put(field, value);
        existsSet.add(table + SEP + field);
    }

    public Object getFieldValue(String table, String field) {
        return getTableMap(table).get(field);
    }

    public Map<String, Object> getTableMap(String table) {
        return contextMap.computeIfAbsent(table, k -> new LinkedHashMap<>());
    }

    public boolean exists(String table, String field) {
        return existsSet.contains(table + SEP + field);
    }

    public void clear() {
        existsSet.clear();
    }

    public void incrementNo() {
        ++currNo;
    }
}
