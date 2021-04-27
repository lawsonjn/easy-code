package com.developcollect.easycode.core.fakedata;

import org.junit.Test;

public class ReferenceDbDataFakerTest {


    @Test
    public void test1() {
        FakeDataContext fakeDataContext = new FakeDataContext(33);
        fakeDataContext.addFieldValue("user", "name", "3333");
        fakeDataContext.addFieldValue("user", "pass", "4444");

        ReferenceDbDataFaker referenceDbDataFaker = new ReferenceDbDataFaker();
        referenceDbDataFaker.setFormat("我是${user.name}和${user.pass}");
        referenceDbDataFaker.init(null);
        Object fakerData = referenceDbDataFaker.getFakerData(fakeDataContext);
        System.out.println(fakerData);
    }

}