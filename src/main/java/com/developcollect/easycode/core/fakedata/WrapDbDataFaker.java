package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/14 10:45
 */
@Data
public class WrapDbDataFaker implements IDbDataFaker {

    private IDbDataFaker dbDataFaker;

    private String format = "前缀${value}";

    @Override
    public void init(FakeDataContext context) {
        dbDataFaker.init(context);
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        return format.replaceAll("\\$\\{value}", String.valueOf(dbDataFaker.getFakerData(context)));
    }

    @Override
    public String toString() {
        return StrUtil.format("模板填充(W:[{}],F:[{}])", dbDataFaker, format);
    }
}
