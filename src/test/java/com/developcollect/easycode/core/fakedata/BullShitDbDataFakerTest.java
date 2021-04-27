package com.developcollect.easycode.core.fakedata;

import com.developcollect.core.utils.DateUtil;
import org.junit.Test;

public class BullShitDbDataFakerTest {

    @Test
    public void test2() {
        BullShitDbDataFaker bullShitDbDataFaker = new BullShitDbDataFaker();
        for (int i = 0; i < 50; i++) {
            System.out.println(bullShitDbDataFaker.getFakerData(null));
        }

    }


    @Test
    public void test3() {
        String s = DateUtil.formatDuration(2222, "HH分钟mm:ss");
        System.out.println(s);
    }





}