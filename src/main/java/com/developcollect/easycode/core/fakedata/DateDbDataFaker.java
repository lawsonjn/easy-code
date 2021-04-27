package com.developcollect.easycode.core.fakedata;

import com.developcollect.core.utils.DateUtil;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/13 10:25
 */
@Data
public class DateDbDataFaker implements IDbDataFaker {

    /**
     * 在指定范围随机
     */
    public static final int TYPE_RANDOM = 1;

    /**
     * 固定值递增或递减
     */
    public static final int TYPE_INCREASE = 2;

    private int type = TYPE_RANDOM;
    private String format = "yyyy-MM-dd HH:mm:ss";

    private String begin;
    private String min;
    private String max;

    private RandomDbDataFaker randomDbDataFaker;
    private IncreaseDbDataFaker increaseDbDataFaker;

    {
        increaseDbDataFaker = new IncreaseDbDataFaker();
        setBegin("1980-01-01 00:00:00");
        setStep(1000 * 60 * 5);
        setStepMin(1000 * 60 * 5L);
        setStepMax(1000 * 60 * 10L);
        setRandomStep(false);

        randomDbDataFaker = new RandomDbDataFaker();
        randomDbDataFaker.setType(RandomDbDataFaker.TYPE_LONG);
        setMin("1980-01-01 00:00:00");
        setMax("2100-01-01 00:00:00");
    }


    @Override
    public void init(FakeDataContext context) {
        increaseDbDataFaker.init(context);
        randomDbDataFaker.init(context);
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (type == TYPE_RANDOM) {
            return randomDate(context);
        } else {
            return increaseDate(context);
        }
    }


    public boolean isRandomStep() {
        return increaseDbDataFaker.isRandomStep();
    }

    public void setRandomStep(boolean randomStep) {
        increaseDbDataFaker.setRandomStep(randomStep);
    }

    public void setBegin(String begin) {
        this.begin = begin;
        increaseDbDataFaker.setBegin(BigDecimal.valueOf(DateUtil.parse(begin, format).getTime()));
    }

    public String getBegin() {
        return begin;
    }

    public void setStep(long step) {
        increaseDbDataFaker.setStep(BigDecimal.valueOf(step));
    }

    public long getStep() {
        return increaseDbDataFaker.getStep().longValue();
    }

    public long getStepMin() {
        return increaseDbDataFaker.getStepMin().longValue();
    }

    public void setStepMin(long stepMin) {
        increaseDbDataFaker.setStepMin(BigDecimal.valueOf(stepMin));
    }

    public long getStepMax() {
        return increaseDbDataFaker.getStepMax().longValue();
    }

    public void setStepMax(long stepMax) {
        increaseDbDataFaker.setStepMax(BigDecimal.valueOf(stepMax));
    }


    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
        randomDbDataFaker.setMin(DateUtil.parse(min, format).getTime());
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
        randomDbDataFaker.setMax(DateUtil.parse(max, format).getTime());
    }

    private String randomDate(FakeDataContext context) {
        long time = (long) randomDbDataFaker.getFakerData(context);
        LocalDateTime dateTime = Instant.ofEpochMilli(time).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return DateUtil.format(dateTime, format);
    }

    private String increaseDate(FakeDataContext context) {
        BigDecimal fakerData = (BigDecimal) increaseDbDataFaker.getFakerData(context);
        LocalDateTime dateTime = Instant.ofEpochMilli(fakerData.longValue()).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return DateUtil.format(dateTime, format);
    }


    @Override
    public String toString() {
        if (type == TYPE_RANDOM) {
            return "日期生成器(随机日期)";
        } else {
            if (isRandomStep()) {
                return "日期生成器(随机递变)";
            } else {
                return "日期生成器(固定递变)";
            }
        }
    }
}
