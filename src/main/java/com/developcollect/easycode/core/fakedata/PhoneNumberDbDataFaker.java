package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:48
 */

public class PhoneNumberDbDataFaker implements IDbDataFaker {

//    private Faker faker = new Faker(new Locale("zh_CN"));
    private static final String[] FORMATS = new String[] {
        "13#########",
        "145########",
        "147########",
        "150########",
        "151########",
        "152########",
        "153########",
        "155########",
        "156########",
        "157########",
        "158########",
        "159########",
        "170########",
        "171########",
        "172########",
        "173########",
        "175########",
        "176########",
        "177########",
        "178########",
        "18#########",
    };


    @Getter
    @Setter
    private String prefix = "";

    private String prefixFormat = null;

    @Override
    public void init(FakeDataContext context) {
        if (StrUtil.isNotBlank(prefix)) {
            prefixFormat = StrUtil.fillAfter(prefix, '#', 11);
        }
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        StringBuilder builder = new StringBuilder(prefixFormat == null ? RandomUtil.randomEle(FORMATS) : prefixFormat);
        int idx = builder.indexOf("#");
        for (int i = idx; i < builder.length(); i++) {
            int n = RandomUtil.randomInt(0, 10);
            builder.replace(i, i + 1, String.valueOf(n));
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "手机号生成器";
    }
}
