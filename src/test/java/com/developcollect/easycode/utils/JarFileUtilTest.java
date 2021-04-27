package com.developcollect.easycode.utils;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.developcollect.core.utils.JarFileUtil;
import org.junit.Assert;
import org.junit.Test;


public class JarFileUtilTest {

    private static final String target = "E:\\laboratory\\tmp";


    @Test
    public void test_01() {
        String dirPath = target + "/01";

        // 复制文件到指定文件夹
        JarFileUtil.copy("/META-INF/LICENSE.txt", dirPath, JSON.class);

        Assert.assertTrue(FileUtil.isFile(dirPath + "/LICENSE.txt"));
    }

    @Test
    public void test_02() {
        // 复制并重命名

        String fullFilepath = target + "/02/a.txt";
        JarFileUtil.copy("/META-INF/LICENSE.txt", fullFilepath, JSON.class, true);
        Assert.assertTrue(FileUtil.exist(fullFilepath));
    }


    @Test
    public void test_03() {
        // 复制整个文件夹

        String dirPath = target + "/03";
        JarFileUtil.copy("/META-INF/", dirPath, JSON.class, true);
        Assert.assertTrue(FileUtil.exist(dirPath));
    }


}