package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 随机值生成
 * 1. 随机数字
 * 2. 随机字符串
 * 3. boolean随机
 * 4. 颜色随机
 * 5. 指定选项随机
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 15:17
 */
@Data
public class RandomDbDataFaker implements IDbDataFaker {

    public static final int TYPE_INT = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_BOOLEAN = 3;
    public static final int TYPE_STRING = 4;
    public static final int TYPE_COLOR = 5;
    public static final int TYPE_OPTION = 6;

    private int type = 1;
    private Long min;
    private Long max;
    private String baseString = RandomUtil.BASE_CHAR_NUMBER;
    private List baseList = new ArrayList();

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (type == TYPE_INT) {
            return randomInt();
        } else if (type == TYPE_LONG) {
            return randomLong();
        } else if (type == TYPE_COLOR) {
            return randomColor();
        } else if (type == TYPE_STRING) {
            return randomString();
        } else if (type == TYPE_OPTION) {
            return randomOption();
        } else if (type == TYPE_BOOLEAN) {
            return randomBoolean();
        }
        throw new IllegalArgumentException("不支持的随机类型：" + type);
    }

    private int randomInt() {
        if (min != null && max != null) {
            return RandomUtil.randomInt(min.intValue(), max.intValue());
        }
        if (min != null) {
            return RandomUtil.randomInt(min.intValue(), Integer.MAX_VALUE);
        }
        if (max != null) {
            return RandomUtil.randomInt(Integer.MIN_VALUE, max.intValue());
        }
        return RandomUtil.randomInt();
    }

    private long randomLong() {
        if (min != null && max != null) {
            return RandomUtil.randomLong(min, max);
        }
        if (min != null) {
            return RandomUtil.randomLong(min, Long.MAX_VALUE);
        }
        if (max != null) {
            return RandomUtil.randomLong(Long.MIN_VALUE, max);
        }
        return RandomUtil.randomLong();
    }

    private String randomColor() {
        int r = RandomUtil.randomInt(256);
        int g = RandomUtil.randomInt(256);
        int b = RandomUtil.randomInt(256);

        return ("#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b)).toUpperCase();
    }

    private String randomString() {
        int strLen;
        if (min != null && max != null) {
            if (min.equals(max)) {
                strLen = min.intValue();
            } else {
                strLen = RandomUtil.randomInt(min.intValue(), max.intValue());
            }
        } else if (min != null) {
            strLen = RandomUtil.randomInt(min.intValue(), 256);
        } else if (max != null) {
            strLen = RandomUtil.randomInt(0, max.intValue());
        } else {
            strLen = RandomUtil.randomInt(0, 256);
        }

        return RandomUtil.randomString(baseString, strLen);
    }

    private Object randomOption() {
        return RandomUtil.randomEle(baseList);
    }

    private boolean randomBoolean() {
        return RandomUtil.randomBoolean();
    }


    @Override
    public String toString() {
        if (type == TYPE_INT) {
            return StrUtil.format("随机整数(MIN:{},MAX:{})", min, max);
        } else if (type == TYPE_LONG) {
            return StrUtil.format("随机长整数(MIN:{},MAX:{})", min, max);
        } else if (type == TYPE_COLOR) {
            return "随机颜色";
        } else if (type == TYPE_STRING) {
            return StrUtil.format("随机字符串(MIN:{},MAX:{})", min, max);
        } else if (type == TYPE_OPTION) {
            return "随机元素";
        } else if (type == TYPE_BOOLEAN) {
            return "随机Boolean";
        }
        throw new IllegalArgumentException("不支持的随机类型：" + type);
    }
}
