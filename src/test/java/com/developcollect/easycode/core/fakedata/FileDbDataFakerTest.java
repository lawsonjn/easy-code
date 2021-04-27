package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

public class FileDbDataFakerTest {

    @Test
    public void test1() {
        FileDbDataFaker fileDbDataFaker = new FileDbDataFaker();
        fileDbDataFaker.init(null);
        fileDbDataFaker.setOnlyFilename(true);
        for (int i = 0; i < 50; i++) {
            System.out.println(fileDbDataFaker.getFakerData(null));
        }

    }

}