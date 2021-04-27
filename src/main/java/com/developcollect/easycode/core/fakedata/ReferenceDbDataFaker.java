package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/14 15:15
 */
public class ReferenceDbDataFaker implements IDbDataFaker {

    /**
     * 对其他字段的引用
     * 我是${xxx.ddd}和${xxx.ddd}
     */
    @Getter
    @Setter
    private String format = "";
    private Set<String> keySet;
    private boolean onlyValue = false;


    @Override
    public void init(FakeDataContext context) {
        keySet = new HashSet<>();
        Pattern pattern = PatternPool.get("\\$\\{(.+?\\..+?)}");

        Matcher matcher = pattern.matcher(format);
        while (matcher.find()) {
            String group = matcher.group(1);
            keySet.add(group);
        }

        if (keySet.size() == 1) {
            String key = keySet.iterator().next();
            if (("${" + key + "}").equals(format)) {
                onlyValue = true;
            }
        }
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        String value = format;
        for (String key : keySet) {
            int dotIdx = key.indexOf(".");
            String table = key.substring(0, dotIdx);
            String field = key.substring(dotIdx + 1);
            if (context.exists(table, field)) {
                Object fieldValue = context.getFieldValue(table, field);
                if (onlyValue) {
                    return fieldValue;
                }
                value = value.replaceAll(ReUtil.escape("${" + key + "}"), String.valueOf(fieldValue));
            } else {
                throw new ReferenceNotFoundException(key);
            }
        }
        return value;
    }


    @Override
    public String toString() {
        return "引用(" + format + ")";
    }
}
