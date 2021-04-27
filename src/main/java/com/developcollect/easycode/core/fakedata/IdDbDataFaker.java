package com.developcollect.easycode.core.fakedata;

import com.developcollect.core.utils.IdUtil;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/12 10:13
 */
@Data
public class IdDbDataFaker implements IDbDataFaker {


    public static final int TYPE_SELF_INCREASING = 1;
    public static final int TYPE_INCREASE = 2;
    public static final int TYPE_UUID = 3;
    public static final int TYPE_SNOWFLAKE_ID = 4;
    public static final int TYPE_SEQUENCE_ID = 5;

    /**
     * id类型
     */
    private int idType = TYPE_SELF_INCREASING;

    /**
     * 递增id
     */
    private IncreaseDbDataFaker increaseDbDataFaker = new IncreaseDbDataFaker();

    private Long workId = 1L;
    private Long datacenterId = 1L;

    @Override
    public void init(FakeDataContext context) {
        if (idType == TYPE_INCREASE) {
            increaseDbDataFaker.init(context);
        }
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (idType == TYPE_SELF_INCREASING) {
            return null;
        }
        // 递增
        if (idType == TYPE_INCREASE) {
            return increaseDbDataFaker.getFakerData(context);
        }
        if (idType == TYPE_UUID) {
            return IdUtil.fastSimpleUUID();
        }
        if (idType == TYPE_SNOWFLAKE_ID) {
            return IdUtil.getSnowflake(workId, datacenterId).nextId();
        }
        if (idType == TYPE_SEQUENCE_ID) {
            if (workId == null || datacenterId == null) {
                return IdUtil.getSequence().nextId();
            } else {
                return IdUtil.getSequence(workId, datacenterId).nextId();
            }
        }
        throw new IllegalArgumentException("无法识别的id类型：" + idType);
    }



    @Override
    public String toString() {
        if (idType == TYPE_SELF_INCREASING) {
            return "Id生成器(自增)";
        }
        if (idType == TYPE_INCREASE) {
            return "Id生成器(递增)";
        }
        if (idType == TYPE_UUID) {
            return "Id生成器(UUID)";
        }
        if (idType == TYPE_SNOWFLAKE_ID) {
            return "Id生成器(雪花ID)";
        }
        if (idType == TYPE_SEQUENCE_ID) {
            return "Id生成器(SequenceID)";
        }
        return "Id生成器";
    }


    public void setBeginId(long beginValue) {
        increaseDbDataFaker.setBegin(BigDecimal.valueOf(beginValue));
    }

    public long getBeginId() {
        return increaseDbDataFaker.getBegin().longValue();
    }

    public void setStep(long step) {
        increaseDbDataFaker.setStep(BigDecimal.valueOf(step));
    }

    public long getStep() {
        return increaseDbDataFaker.getStep().longValue();
    }
}
