package com.developcollect.easycode;

import com.developcollect.easycode.utils.UpdateUtil;
import org.junit.Test;

import java.util.List;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/3 17:22
 */
public class MiscTest {


    @Test
    public void test() {
        List<String> list = UpdateUtil.getUpdateTips();

        for (String s : list) {
            System.out.println(s);
        }
    }
}
