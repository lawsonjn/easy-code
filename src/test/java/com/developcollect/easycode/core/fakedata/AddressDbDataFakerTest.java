package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import org.junit.Test;

import java.util.Locale;

public class AddressDbDataFakerTest {

    @Test
    public void test1() {
        AddressDbDataFaker addressDbDataFaker = new AddressDbDataFaker();
        for (int i = 0; i < 10; i++) {
            System.out.println(addressDbDataFaker.getFakerData(null));
        }
    }

    @Test
    public void test2() {
        Faker faker = new Faker(new Locale("zh_CN"));
        Address address = faker.address();

        System.out.println(address.streetAddress());
        System.out.println(address.city());
        System.out.println(address.state());
        System.out.println(address.countryCode());
        System.out.println(address.zipCode());
        System.out.println(address.stateAbbr());
    }


    @Test
    public void test3() {
        Faker faker = new Faker(new Locale("zh_CN"));

        for (int i = 0; i < 50; i++) {
            System.out.println(faker.idNumber().validSvSeSsn());
        }

    }

}