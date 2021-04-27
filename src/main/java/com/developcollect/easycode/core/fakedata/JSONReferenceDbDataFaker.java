package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.developcollect.core.utils.LambdaUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 14:20
 */
public class JSONReferenceDbDataFaker implements IDbDataFaker {

    private static Map<String, JSONArray> jsonMap = new HashMap<>();
    private static Map<String, String> md5Map = new HashMap<>();



    /**
     * 对JSON字段的引用
     * 我是${xxx.ddd}和${xxx.ddd}
     */
    @Getter
    @Setter
    private String format = "";
    @Getter
    @Setter
    private String jsonPath;
    @Getter
    @Setter
    private boolean cycle = true;

    private int idx = 0;

    private Set<String> keySet;
    private boolean onlyValue = false;

    @Override
    public void init(FakeDataContext context) {
        keySet = new HashSet<>();
        Pattern pattern = PatternPool.get("\\$\\{(.+?)}");

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


        if (!FileUtil.isFile(jsonPath)) {
            LambdaUtil.raise(new FileNotFoundException(jsonPath));
        }
        idx = 0;
        String md5 = SecureUtil.md5(new File(jsonPath));
        if (jsonMap.containsKey(jsonPath) && md5.equals(md5Map.get(jsonPath))) {
            return;
        }

        JSONArray jsonArray;
        Object o = JSON.parse(FileUtil.readUtf8String(jsonPath));
        if (o instanceof JSONArray) {
            jsonArray = (JSONArray) o;
        } else {
            jsonArray = new JSONArray();
            jsonArray.add(o);
        }
        jsonMap.put(jsonPath, jsonArray);
        md5Map.put(jsonPath, md5);
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        JSONArray array = getArray();
        if (cycle && array.size() == idx) {
            idx = 0;
        }
        Object o = getArray().get(idx++);

        String value = format;
        for (String key : keySet) {
            Object property = BeanUtil.getProperty(o, key);
            if (onlyValue) {
                return property;
            }
            value = value.replaceAll(ReUtil.escape("${" + key + "}"), String.valueOf(property));
        }
        return value;
    }


    private JSONArray getArray() {
        return jsonMap.get(jsonPath);
    }

    @Override
    public String toString() {
        return "JSON引用";
    }
}
