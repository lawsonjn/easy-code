package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

public class PhoneNumberDbDataFakerTest {

    @Test
    public void test1() {
        PhoneNumberDbDataFaker faker = new PhoneNumberDbDataFaker();

        for (int i = 0; i < 50; i++) {
            System.out.println(faker.getFakerData(null));
        }

    }

    @Test
    public void test2() {
        PhoneNumberDbDataFaker faker = new PhoneNumberDbDataFaker();
        faker.setPrefix("137");
        faker.init(null);

        for (int i = 0; i < 50; i++) {
            System.out.println(faker.getFakerData(null));
        }

    }

}