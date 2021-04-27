package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

public class EmailDbDataFakerTest {

    @Test
    public void test() {
        EmailDbDataFaker emailDbDataFaker = new EmailDbDataFaker();
        for (int i = 0; i < 50; i++) {
            System.out.println(emailDbDataFaker.getFakerData(null));
        }
    }
}