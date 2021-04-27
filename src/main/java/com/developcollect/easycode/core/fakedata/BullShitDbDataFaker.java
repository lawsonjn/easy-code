package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/13 12:07
 */
public class BullShitDbDataFaker implements IDbDataFaker {

    private String[] p1 = new String[]{"", "我", "你", "他", "刘备", "张飞", "秦始皇", "李云龙", "它"};
    private String[] p2 = new String[]{"", "想", "要"};
    private String[] p3 = new String[]{"做", "打", "推", "拉", "拽", "扫", "学", "砍", "去", "炸", "吃", "喝"};
    private String[] p4 = new String[]{
            "区块链", "人工智能", "编程", "饭", "菜", "搞基", "树", "蛋糕", "炒币", "萝卜", "佛跳墙", "午睡",
            "火星", "外太空", "奥特曼", "蝙蝠侠", "外星人", "白宫", "五角大楼", "布丁", "可乐", "宫保鸡丁"
    };

    @Override
    public Object getFakerData(FakeDataContext context) {
        return BullShit.generator(randomTitle(), RandomUtil.randomInt(5000, 20000));
    }


    private String randomTitle() {
        return RandomUtil.randomEle(p1) + RandomUtil.randomEle(p2) + RandomUtil.randomEle(p3) + RandomUtil.randomEle(p4);
    }

    @Override
    public String toString() {
        return "狗屁不通文章生成器";
    }
}
