package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import org.junit.Test;

import java.util.Locale;

public class CompanyDbDataFakerTest {


    @Test
    public void test1() {
        CompanyDbDataFaker companyDbDataFaker = new CompanyDbDataFaker();
        for (int i = 0; i < 50; i++) {
            System.out.println(companyDbDataFaker.getFakerData(null));
        }
    }

    @Test
    public void test2() {
        Faker faker = new Faker(new Locale("zh_CN"));

        System.out.println(faker.weather().description());
    }

}