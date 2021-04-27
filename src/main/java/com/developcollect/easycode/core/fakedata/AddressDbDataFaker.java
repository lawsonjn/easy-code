package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import lombok.Data;

import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:59
 */
@Data
public class AddressDbDataFaker implements IDbDataFaker {

    /**
     * 省
     */
    public static final int TYPE_PROVINCE = 1;
    /**
     * 省缩写
     */
    public static final int TYPE_PROVINCE_ABBR = 2;
    /**
     * 市
     */
    public static final int TYPE_CITY = 3;
    /**
     * 邮编
     */
    public static final int TYPE_POSTCODE = 4;
    /**
     * 地址
     */
    public static final int TYPE_ADDRESS = 5;

    /**
     * 省+市+地址
     */
    public static final int TYPE_FULL_ADDRESS = 6;

    private int type = TYPE_ADDRESS;


    private transient Faker faker = new Faker(new Locale("zh_CN"));


    @Override
    public Object getFakerData(FakeDataContext context) {
        Address address = faker.address();
        switch (type) {
            case TYPE_PROVINCE:
                return address.state();
            case TYPE_PROVINCE_ABBR:
                return address.stateAbbr();
            case TYPE_CITY:
                return address.city();
            case TYPE_POSTCODE:
                return address.zipCode();
            case TYPE_ADDRESS:
                return address.streetAddress();
            case TYPE_FULL_ADDRESS:
                return address.state() + address.city() + address.streetAddress();
            default:
        }
        throw new IllegalArgumentException("不支持的生成类型：" + type);
    }



    @Override
    public String toString() {
        return "地址生成器";
    }
}
