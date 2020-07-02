package com.alan.socketproxy.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * @author chenxinhui
 * @Description: socket客户端
 * @date 2020/7/2
 */
public class Socket5ProxyClientUtil {

    private static String address = "127.0.0.1";
    private static int port = 8888;

    /**
     * 单元测试
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        InetSocketAddress remoteAddress = new InetSocketAddress("192.168.60.132", 8787);
//        InetSocketAddress remoteAddress = new InetSocketAddress("10.1.8.20", 32010);
        Socket5ProxyClientUtil.start(remoteAddress);
    }


    /**
     * 程序入口
     *
     * @param remoteAddress 远程访问配置
     * @throws IOException
     */
    public static void start(InetSocketAddress remoteAddress) throws IOException {
        Socket socket = null;
        try {
            InetSocketAddress proxyAddress = new InetSocketAddress(address, port);
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);
            socket = new Socket(proxy);
            socket.connect(remoteAddress);
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }

        }
    }
}
