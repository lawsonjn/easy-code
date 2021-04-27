package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/13 9:29
 */
public class EmailDbDataFaker implements IDbDataFaker {


    private String[] p2 = new String[]{"qq", "163", "outlook", "126", "foxmail", "139", "sina", "gmail", "msn", "hotmail", "live"};
    private String[] p3 = new String[]{"com", "cn", "net", "xyz"};

    @Override
    public Object getFakerData(FakeDataContext context) {
        String s1;
        String s2 = RandomUtil.randomEle(p2);
        String s3 = RandomUtil.randomEle(p3);
        if ("qq".equals(s2)) {
            s1 = RandomUtil.randomString(RandomUtil.BASE_NUMBER, RandomUtil.randomInt(8, 10));
        } else {
            s1 = RandomUtil.randomString(RandomUtil.BASE_CHAR, RandomUtil.randomInt(2, 5))
                    + RandomUtil.randomString(RandomUtil.BASE_NUMBER, RandomUtil.randomInt(3, 20));
        }

        return s1 + "@" + s2 + "." + s3;
    }


    @Override
    public String toString() {
        return "邮箱号";
    }
}
