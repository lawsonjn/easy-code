package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import lombok.Data;

import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 13:35
 */
@Data
public class IpDbDataFaker implements IDbDataFaker {
    private transient Faker faker = new Faker(new Locale("zh_CN"));


    private boolean ipv4 = true;
    private boolean privateIp = true;
    private boolean cidr = false;

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (ipv4) {
            if (privateIp) {
                return faker.internet().privateIpV4Address();
            } else {
                return faker.internet().publicIpV4Address();
            }
        } else {
            return faker.internet().ipV6Address();
        }
    }

    @Override
    public String toString() {
        if (ipv4) {
            if (privateIp) {
                return "IP生成器(IPv4,内网)";
            } else {
                return "IP生成器(IPv4,外网)";
            }
        } else {
            return "IP生成器(IPv6)";
        }
    }
}
