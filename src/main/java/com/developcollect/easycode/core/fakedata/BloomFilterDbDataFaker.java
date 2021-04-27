package com.developcollect.easycode.core.fakedata;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import cn.hutool.bloomfilter.BloomFilterUtil;
import lombok.Data;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 9:03
 */
@Data
public class BloomFilterDbDataFaker implements IDbDataFaker {

    private BitMapBloomFilter bitMap;

    private IDbDataFaker dbDataFaker;

    private long maxRetry = 1000000;

    @Override
    public void init(FakeDataContext context) {
        bitMap = BloomFilterUtil.createBitMap(Math.max((int) context.getTotal() / (1024 * 1024 * 8), 1) * 5);
        dbDataFaker.init(context);
    }

    @Override
    public Object getFakerData(FakeDataContext context) {
        for (long i = 0; maxRetry <= 0 || i < maxRetry; i++) {
            Object fakerData = dbDataFaker.getFakerData(context);
            if (fakerData == null) {
                return null;
            }
            String str = fakerData.toString();
            if (!bitMap.contains(str)) {
                bitMap.add(str);
                return fakerData;
            }
        }
        // 超过1百万次还没生成能用的数据，则可能是内部生成器有问题
        throw new RuntimeException("布隆过滤超过" + getMaxRetry() + "次");
    }

    @Override
    public String toString() {
        return "布隆过滤(" + getDbDataFaker() + ")";
    }
}
