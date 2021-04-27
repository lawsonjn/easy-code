package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

public class JSONReferenceDbDataFakerTest {


    @Test
    public void test1() {
        JSONReferenceDbDataFaker dbDataFaker = new JSONReferenceDbDataFaker();
        dbDataFaker.setJsonPath("E:\\laboratory\\tmp\\bbb.json");
        dbDataFaker.setFormat("我是${[1].class}和${[0].name}");
        dbDataFaker.init(null);


        System.out.println(dbDataFaker.getFakerData(null));
    }

}