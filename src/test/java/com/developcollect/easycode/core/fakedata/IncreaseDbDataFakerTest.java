package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

import java.math.BigDecimal;

public class IncreaseDbDataFakerTest {


    @Test
    public void test1() {
        IncreaseDbDataFaker increaseDbDataFaker = new IncreaseDbDataFaker();
        increaseDbDataFaker.setBegin(BigDecimal.valueOf(0));
        increaseDbDataFaker.setRandomStep(true);
        increaseDbDataFaker.setStepMin(BigDecimal.valueOf(1));
        increaseDbDataFaker.setStepMax(BigDecimal.valueOf(5));

        increaseDbDataFaker.init(null);

        for (int i = 0; i < 50; i++) {
            System.out.println(increaseDbDataFaker.getFakerData(null));
        }
    }

}