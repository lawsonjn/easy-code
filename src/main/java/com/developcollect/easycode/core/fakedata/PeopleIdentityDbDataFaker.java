package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:56
 */
public class PeopleIdentityDbDataFaker implements IDbDataFaker {

    @Override
    public Object getFakerData(FakeDataContext context) {
        return PeopleIdGen.getIdNo(RandomUtil.randomBoolean());
    }

    @Override
    public String toString() {
        return "身份证号";
    }
}
