package com.developcollect.easycode.core.fakedata;

import com.github.javafaker.Faker;
import com.github.javafaker.Internet;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 13:27
 */
public class UserAgentDbDataFaker implements IDbDataFaker {
    public static final int USER_AGENT_TYPE_ANY = 0;
    public static final int USER_AGENT_TYPE_AOL = 1;
    public static final int USER_AGENT_TYPE_CHROME = 2;
    public static final int USER_AGENT_TYPE_FIREFOX = 3;
    public static final int USER_AGENT_TYPE_INTERNET_EXPLORER = 4;
    public static final int USER_AGENT_TYPE_NETSCAPE = 5;
    public static final int USER_AGENT_TYPE_OPERA = 6;
    public static final int USER_AGENT_TYPE_SAFARI = 7;

    private transient Faker faker = new Faker(new Locale("zh_CN"));

    @Getter
    @Setter
    private int userAgentType = USER_AGENT_TYPE_ANY;

    @Override
    public Object getFakerData(FakeDataContext context) {
        String userAgent;
        switch (userAgentType) {
            case USER_AGENT_TYPE_AOL:
                userAgent = faker.internet().userAgent(Internet.UserAgent.AOL);
                break;
            case USER_AGENT_TYPE_CHROME:
                userAgent = faker.internet().userAgent(Internet.UserAgent.CHROME);
                break;
            case USER_AGENT_TYPE_FIREFOX:
                userAgent = faker.internet().userAgent(Internet.UserAgent.FIREFOX);
                break;
            case USER_AGENT_TYPE_INTERNET_EXPLORER:
                userAgent = faker.internet().userAgent(Internet.UserAgent.INTERNET_EXPLORER);
                break;
            case USER_AGENT_TYPE_NETSCAPE:
                userAgent = faker.internet().userAgent(Internet.UserAgent.NETSCAPE);
                break;
            case USER_AGENT_TYPE_OPERA:
                userAgent = faker.internet().userAgent(Internet.UserAgent.OPERA);
                break;
            case USER_AGENT_TYPE_SAFARI:
                userAgent = faker.internet().userAgent(Internet.UserAgent.SAFARI);
                break;
            default:
            case USER_AGENT_TYPE_ANY:
                userAgent = faker.internet().userAgentAny();
                break;
        }
        return userAgent;
    }

    @Override
    public String toString() {
        switch (userAgentType) {
            case USER_AGENT_TYPE_AOL:
                return "UserAgent(AOL)";
            case USER_AGENT_TYPE_CHROME:
                return "UserAgent(chrome)";
            case USER_AGENT_TYPE_FIREFOX:
                return "UserAgent(firefox)";
            case USER_AGENT_TYPE_INTERNET_EXPLORER:
                return "UserAgent(IE)";
            case USER_AGENT_TYPE_NETSCAPE:
                return "UserAgent(netscape)";
            case USER_AGENT_TYPE_OPERA:
                return "UserAgent(opera)";
            case USER_AGENT_TYPE_SAFARI:
                return "UserAgent(safari)";
            default:
            case USER_AGENT_TYPE_ANY:
                return "UserAgent(Any)";
        }

    }
}
