package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 递增或递减数值生成器具
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 15:08
 */
@Data
public class IncreaseDbDataFaker implements IDbDataFaker {

    private BigDecimal begin = BigDecimal.valueOf(0);
    private BigDecimal step = BigDecimal.valueOf(1);
    private BigDecimal prevValue = begin.subtract(step);

    private BigDecimal stepMin = BigDecimal.valueOf(1);
    private BigDecimal stepMax = BigDecimal.valueOf(10);

    private boolean randomStep = false;

    @Override
    public void init(FakeDataContext context) {
        initPrevValue();
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (randomStep) {
            prevValue = prevValue.add(RandomUtil.randomBigDecimal(stepMin, stepMax));
        } else {
            prevValue = prevValue.add(step);
        }
        return prevValue;
    }

    private void initPrevValue() {
        prevValue = this.begin.subtract(step);
    }

    @Override
    public String toString() {
        if (randomStep) {
            return StrUtil.format("递变数值(B:{},S1:{},S2:{})", begin, stepMin, stepMax);
        } else {
            return StrUtil.format("递变数值(B:{},S:{})", begin, step);
        }
    }
}
