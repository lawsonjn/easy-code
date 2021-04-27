package com.developcollect.easycode.core.fakedata;

import lombok.Data;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 14:07
 */
@Data
public class ConstantDbDataFaker implements IDbDataFaker {
    private String constant = "";


    @Override
    public Object getFakerData(FakeDataContext context) {
        return constant;
    }

    @Override
    public String toString() {
        return "固定值(" + constant + ")";
    }
}
