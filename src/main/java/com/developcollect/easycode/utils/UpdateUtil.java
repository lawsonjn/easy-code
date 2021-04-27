package com.developcollect.easycode.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.comparator.VersionComparator;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpGlobalConfig;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.developcollect.core.utils.LambdaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/2/25 14:16
 */
public class UpdateUtil {
    public static final List<String> DOWNLOAD_URLS = CollUtil.newArrayList("http://192.168.0.86/easycode/EasyCode-%s.jar", "http://a690710.55555.io/easycode/EasyCode-%s.jar");

    public static final List<String> LAST_VERSION_URLS = CollUtil.newArrayList("http://192.168.0.86/easycode/lastVersion", "http://a690710.55555.io/easycode/lastVersion");
    private static final Logger log = LoggerFactory.getLogger(UpdateUtil.class);


    // 检测是否有新的文件
    // 下载新的执行文件
    // 替换原有的文件
    // 提示重启

    public static void hasUpdateAsync(Consumer<Boolean> consumer) {
        ThreadUtil.execAsync(() -> consumer.accept(VersionComparator.INSTANCE.compare(BuildVersion.getVersion(), getRemoteVersion()) < 0));
    }

    public static boolean hasUpdate() {
        return VersionComparator.INSTANCE.compare(BuildVersion.getVersion(), getRemoteVersion()) < 0;
    }


    private static String getRemoteVersion() {
        for (String lastVersionUrl : LAST_VERSION_URLS) {
            try {
                HttpResponse response = HttpRequest.get(lastVersionUrl).timeout(HttpGlobalConfig.getTimeout()).execute();
                if (response.isOk()) {
                    String body = response.body();
                    if (StrUtil.isNotBlank(body)) {
                        return body;
                    }
                }
            } catch (Exception ignore) {
            }
        }

        return null;
    }

    public static File downloadUpdate() {
        File tempFile = FileUtil.createTempFile(FileUtil.getTmpDir());
        String remoteVersion = getRemoteVersion();

        for (String downloadUrl : DOWNLOAD_URLS) {
            try {
                HttpUtil.downloadFile(String.format(downloadUrl, remoteVersion), tempFile, null);
                return tempFile;
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    public static void doUpdate() {
        File tempFile = FileUtil.createTempFile(FileUtil.getTmpDir());
        String tmpPath = FileUtil.getAbsolutePath(tempFile);
        String location = Util.getLocation();
        FileUtil.copy(location, tmpPath, true);

        String cmd = String.format("javaw -Dfile.encoding=utf-8 -jar %s update %s ", tmpPath, location);
        // 停掉自己
        // 启动一个新的进程实现更新
        RuntimeUtil.exec(cmd);
        System.exit(1);
    }


    public static List<String> getUpdateTips() {
        try (InputStream inputStream = UpdateUtil.class.getClassLoader().getResourceAsStream("misc/update.info")) {
            if (inputStream == null) {
                throw new IllegalStateException("无法获取更新信息");
            }

            List<String> list = new LinkedList<>();
            IoUtil.readUtf8Lines(inputStream, new LineHandler() {
                boolean versionFind = false;
                boolean over = false;

                @Override
                public void handle(String line) {
                    if (over) {
                        return;
                    }
                    if (versionFind) {
                        if (line.startsWith("--")) {
                            over = true;
                            return;
                        }
                        list.add(line);
                        return;
                    }
                    if (line.equals("【" + BuildVersion.getVersion() + "】")) {
                        list.add(line);
                        versionFind = true;
                    }
                }
            });
            return list;
        } catch (IOException e) {
            log.error("读取更新信息失败", e);
            return LambdaUtil.raise(e);
        }
    }

}
