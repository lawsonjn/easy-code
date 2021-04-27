package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import lombok.Data;

import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:41
 */
@Data
public class NameDbDataFaker implements IDbDataFaker {

    public static final int PART_FULL_NAME = 1;
    public static final int PART_FIRST_NAME = 2;
    public static final int PART_LAST_NAME = 3;

    private int namePart = 1;

    private transient Faker faker = new Faker(new Locale("zh_CN"));

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (namePart == PART_FIRST_NAME) {
            return faker.name().firstName();
        } else if (namePart == PART_LAST_NAME) {
            return faker.name().lastName();
        } else {
            return faker.name().fullName();
        }
    }

    @Override
    public String toString() {
        if (namePart == PART_FIRST_NAME) {
            return "姓名生成器(姓)";
        } else if (namePart == PART_LAST_NAME) {
            return "姓名生成器(名)";
        } else {
            return "姓名生成器(姓名)";
        }

    }
}
