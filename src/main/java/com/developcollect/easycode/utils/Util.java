package com.developcollect.easycode.utils;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.developcollect.easycode.EasyCodeConfig;

import java.io.File;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 15:21
 */
public class Util {

    public static String getLocation() {
        String path = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        OsInfo osInfo = SystemUtil.getOsInfo();
        if (osInfo.isWindows()) {
            return path.substring(1);
        } else {
            return path;
        }
    }

    public static File getLogFile() {
        return new File(EasyCodeConfig.EASY_CODE_CONFIG.getHomeDir() + "/logs/EasyCode.log");
    }
}
