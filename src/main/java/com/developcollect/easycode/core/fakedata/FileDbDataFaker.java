package com.developcollect.easycode.core.fakedata;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.github.javafaker.Faker;
import lombok.Data;

import java.io.File;
import java.util.Locale;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/4/16 14:19
 */
@Data
public class FileDbDataFaker implements IDbDataFaker {

    public static final int TYPE_ONLY_FILENAME = 1;

    // 文件后缀
    public static final String[] DEFAULT_SUFFIXES = new String[] {
            "flac", "mp3", "wav", "bmp", "gif", "jpeg", "jpg", "png", "tiff", "css", "csv",
            "html", "js", "json", "txt", "mp4", "avi", "mov", "webm", "doc", "docx", "xls",
            "xlsx", "ppt", "pptx", "odt", "ods", "odp", "pages", "numbers", "key", "pdf",
            "zip", "rar", "tar", "gz", "tar.gz", "rmvb", "flv", "exe", "ico", "conf",
    };

    public static String[] DEFAULT_WIN_ROOTS = new String[] {"C:", "D:", "E:", "F:"};
    public static String[] DEFAULT_LINUX_ROOTS = new String[] {"/"};



    private transient Faker faker = new Faker(new Locale("zh_CN"));

    private String separator = File.separator;
    private String[] suffixes = DEFAULT_SUFFIXES;
    private String[] roots;


    @Override
    public void init(FakeDataContext context) {
        if (roots == null) {
            OsInfo osInfo = SystemUtil.getOsInfo();
            if (osInfo.isWindows()) {
                roots = new String[] {"C:", "D:", "E:", "F:"};
            } else {
                if ("/".equals(separator)) {
                    roots = new String[] {""};
                } else {
                    roots = new String[] {"/"};
                }
            }
        }
    }


    private boolean onlyFilename = false;

    @Override
    public Object getFakerData(FakeDataContext context) {
        if (onlyFilename) {
            final String name = faker.lorem().word().toLowerCase();
            final String ext = RandomUtil.randomEle(suffixes);
            return name + "." + ext;
        } else {
            final String root = roots.length == 1 ? roots[0] : RandomUtil.randomEle(roots);
            final String sep = separator;
            final String dir = faker.internet().slug();
            final String name = faker.lorem().word().toLowerCase();
            final String ext = RandomUtil.randomEle(suffixes);
            return root + sep + dir + sep + name + "." + ext;
        }
    }


    @Override
    public String toString() {
        return "文件生成器";
    }
}
