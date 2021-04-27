package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import org.junit.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BloomFilterDbDataFakerTest {


    @Test
    public void test1() {
        FakeDataContext context = new FakeDataContext(5000);
        BloomFilterDbDataFaker faker = new BloomFilterDbDataFaker();
        PhoneNumberDbDataFaker phoneNumberDbDataFaker = new PhoneNumberDbDataFaker();
        faker.setDbDataFaker(phoneNumberDbDataFaker);
        faker.setMaxRetry(-1);

        phoneNumberDbDataFaker.setPrefix("137080");
        faker.init(context);





        Set<String> set1 = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            set1.add(phoneNumberDbDataFaker.getFakerData(context).toString());
        }
        Set<String> set2 = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            set2.add(faker.getFakerData(context).toString());
        }

        System.out.println(1);

    }


    @Test
    public void test2() {
        Faker faker = new Faker(new Locale("zh_CN"));
        System.out.println(faker.internet().ipV4Cidr());
    }

}