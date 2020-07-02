package com.alan.socketProxy.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author chenxinhui
 * @Description:
 * @date 2020/6/29
 */
public class Socks5ProxyServer {

    /**
     * 服务监听在哪个端口上
     */
    public static final Integer SERVICE_LISTENER_PORT = 8888;


    public static void main(String[] args) throws IOException {
        Socks5ProxyServer server = new Socks5ProxyServer();
        ServerSocket serverSocket = new ServerSocket(SERVICE_LISTENER_PORT);
        server.doRequest(serverSocket);
    }

    /**
     * 服务入口
     *
     * @param serverSocket
     * @throws IOException
     */
    public void doRequest(ServerSocket serverSocket) throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ServerHandler(socket)).start();
        }
    }


}
