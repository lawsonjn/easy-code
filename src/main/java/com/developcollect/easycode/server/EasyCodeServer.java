package com.developcollect.easycode.server;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ArrayUtil;
import com.developcollect.core.thread.ThreadUtil;
import com.developcollect.easycode.EasyCodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Zhu KaiXiao
 * @version 1.0
 * @date 2021/3/22 17:16
 */
public class EasyCodeServer {

    public static final int PORT = 56168;
    private static final Logger log = LoggerFactory.getLogger(EasyCodeServer.class);
    private static final String checkStr = "EasyCode:PING";
    private static final String checkReply = "EasyCode:PONG";
    private static final String USED_PORT_PATH = EasyCodeConfig.EASY_CODE_CONFIG.getHomeDir() + File.separator + "used_port.txt";
    private static EasyCodeServer easyCodeServer = null;

    private EasyCodeServer() {
        ThreadUtil.execAsync(this::startServer, true);
    }


    public static EasyCodeServer bind() {
        if (easyCodeServer == null) {
            easyCodeServer = new EasyCodeServer();
        }
        return easyCodeServer;
    }

    public static boolean checkStarted() {
        Socket socket = new Socket();
        if (FileUtil.exist(USED_PORT_PATH)) {
            String str = FileUtil.readUtf8String(USED_PORT_PATH);
            try {
                int port = Integer.parseInt(str);
                if (NetUtil.isValidPort(port)) {
                    try {
                        socket.connect(new InetSocketAddress("127.0.0.1", port), 1000);
                    } catch (IOException e) {
                    }
                }
            } catch (NumberFormatException ignore) {
            }
        }
        if (!socket.isConnected()) {
            try {
                socket.connect(new InetSocketAddress("127.0.0.1", PORT), 1000);
            } catch (IOException ignore) {
            }
        }

        // 没有建立连接，说明EasyCode没启动过
        if (!socket.isConnected()) {
            return false;
        }

        // 建立连接了，也有可能是其他的程序占用了端口

        try {
            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                out.write(checkStr.getBytes());
                out.flush();
                final Future<Boolean> booleanFuture = ThreadUtil.execAsync(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            byte[] buff = new byte[1024];
                            int readLen = in.read(buff);
                            if (readLen != 13) {
                                return false;
                            }

                            return checkReply.equals(new String(ArrayUtil.sub(buff, 0, readLen)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                });

                ThreadUtil.waitForDone(booleanFuture, 3000);
                if (booleanFuture.isDone()) {
                    return booleanFuture.get();
                } else {
                    return false;
                }
            }
        } catch (Throwable throwable) {

        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }


        return false;
    }

    private void startServer() {

        ServerSocket serverSocket = null;
        try {
            serverSocket = bind(PORT);
            FileUtil.del(USED_PORT_PATH);
        } catch (IOException ignore) {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignore2) {
                }
            }
            int randomPort = NetUtil.getUsableLocalPort();
            try {
                serverSocket = bind(randomPort);
                // 写入文件
                FileUtil.writeBytes(String.valueOf(randomPort).getBytes(), USED_PORT_PATH);
            } catch (IOException e) {
                log.error("EasyCodeServer启动失败", e);
            } finally {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException ignore2) {
                    }
                }
            }
        }

        if (serverSocket == null) {
            System.exit(1);
        }

        while (true) {
            try (Socket socket = serverSocket.accept()) {
                try (InputStream in = socket.getInputStream();
                     OutputStream out = socket.getOutputStream()) {
                    byte[] buff = new byte[1024];
                    int readLen = in.read(buff);

                    if (readLen == 13 && checkStr.equals(new String(ArrayUtil.sub(buff, 0, readLen)))) {
                        out.write(checkReply.getBytes());
                        out.flush();
                        in.read(buff);
                    }

                }
            } catch (IOException ignore) {
            }
        }
    }

    private ServerSocket bind(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port));
        return serverSocket;
    }
}
