package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import lombok.Data;

import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:57
 */
@Data
public class UniversityDbDataFaker implements IDbDataFaker {
    private transient Faker faker = new Faker(new Locale("zh_CN"));

    @Override
    public Object getFakerData(FakeDataContext context) {

        return faker.university().name();
    }

    @Override
    public String toString() {
        return "高校生成器";
    }
}
