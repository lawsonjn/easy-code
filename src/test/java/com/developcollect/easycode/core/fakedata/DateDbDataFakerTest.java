package com.developcollect.easycode.core.fakedata;

import org.junit.Test;


public class DateDbDataFakerTest {


    @Test
    public void test1() {
        DateDbDataFaker dateDbDataFaker = new DateDbDataFaker();
        for (int i = 0; i < 50; i++) {
            System.out.println(dateDbDataFaker.getFakerData(null));
        }
    }


    @Test
    public void test2() {
        DateDbDataFaker dateDbDataFaker = new DateDbDataFaker();
        dateDbDataFaker.setMin("2020-11-11 00:00:00");
        dateDbDataFaker.setMax("2022-11-11 00:00:00");
        for (int i = 0; i < 50; i++) {
            System.out.println(dateDbDataFaker.getFakerData(null));
        }
    }

    @Test
    public void test3() {
        DateDbDataFaker dateDbDataFaker = new DateDbDataFaker();
        dateDbDataFaker.setType(DateDbDataFaker.TYPE_INCREASE);
        dateDbDataFaker.setStep(1000 * 60 * 3);
        for (int i = 0; i < 50; i++) {
            System.out.println(dateDbDataFaker.getFakerData(null));
        }
    }

    @Test
    public void test4() {
        DateDbDataFaker dateDbDataFaker = new DateDbDataFaker();
        dateDbDataFaker.setType(DateDbDataFaker.TYPE_INCREASE);
        dateDbDataFaker.setRandomStep(true);
        dateDbDataFaker.setStepMin(1000 * 60 * 3);
        dateDbDataFaker.setStepMax(1000 * 60 * 8);
        for (int i = 0; i < 50; i++) {
            System.out.println(dateDbDataFaker.getFakerData(null));
        }
    }

}