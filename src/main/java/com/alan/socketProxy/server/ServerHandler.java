package com.alan.socketproxy.server;

import com.alan.socketproxy.enums.AddressType;
import com.alan.socketproxy.enums.Command;
import com.alan.socketproxy.enums.CommandStatus;
import com.alan.socketproxy.enums.Method;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenxinhui
 * @Description:
 * @date 2020/7/2
 */
public class ServerHandler implements Runnable {



    /**
     * socks协议的版本，固定为5
     */
    private static final byte VERSION = 0X05;
    /**
     * RSV，必须为0
     */
    private static final byte RSV = 0X00;
    /**
     * 用于统计客户端的数量
     */
    private static String SERVER_IP_ADDRESS;

    static {
        try {
            SERVER_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private Socket clientSocket;
    private String clientIp;
    private int clientPort;

    public ServerHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientIp = clientSocket.getInetAddress().getHostAddress();
        this.clientPort = clientSocket.getPort();
    }

    public synchronized static void log(String format, Object... args) {
        System.out.println(String.format(format, args));
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        try {
            // 协商认证方法
            negotiationCertificationMethod();
            // 开始处理客户端的命令
            handleClientCommand();
        } catch (Exception e) {
            handleLog("exception, " + e.getMessage());
        } finally {
            close(clientSocket);
        }
    }

    /**
     * 协商与客户端的认证方法
     * @throws IOException
     */
    private void negotiationCertificationMethod() throws IOException {
        InputStream is = clientSocket.getInputStream();
        OutputStream os = clientSocket.getOutputStream();
        byte[] buff = new byte[255];
        // 接收客户端的支持的方法
        is.read(buff, 0, 2);
        int version = buff[0];
        int methodNum = buff[1];

        if (version != VERSION) {
            throw new RuntimeException("version must 0X05");
        } else if (methodNum < 1) {
            throw new RuntimeException("method num must gt 0");
        }

        is.read(buff, 0, methodNum);
        List<Method> clientSupportMethodList = Method.convertToMethod(Arrays.copyOfRange(buff, 0, methodNum));
        handleLog("version=%s, methodNum=%s, clientSupportMethodList=%s", version, methodNum, clientSupportMethodList);

        // 向客户端发送回应，这里不进行认证
        buff[0] = VERSION;
        buff[1] = Method.NO_AUTHENTICATION_REQUIRED.getRangeStart();
        os.write(buff, 0, 2);
        os.flush();
    }

    /**
     * 认证通过，开始处理客户端发送过来的指令
     *
     * @throws IOException
     */
    private void handleClientCommand() throws IOException {
        InputStream is = clientSocket.getInputStream();
        OutputStream os = clientSocket.getOutputStream();
        byte[] buff = new byte[255];
        // 接收客户端命令
        is.read(buff, 0, 4);
        int version = buff[0];
        Command command = Command.convertToCmd(buff[1]);
        int rsv = buff[2];
        AddressType addressType = AddressType.convertToAddressType(buff[3]);
        if (rsv != RSV) {
            throw new RuntimeException("RSV must 0X05");
        } else if (version != VERSION) {
            throw new RuntimeException("VERSION must 0X05");
        } else if (command == null) {
            // 不支持的命令
            sendCommandResponse(CommandStatus.COMMAND_NOT_SUPPORTED);
            handleLog("not supported command");
            return;
        } else if (addressType == null) {
            // 不支持的地址类型
            sendCommandResponse(CommandStatus.ADDRESS_TYPE_NOT_SUPPORTED);
            handleLog("address type not supported");
            return;
        }

        String targetAddress = "";
        switch (addressType) {
            case DOMAIN:
                // 如果是域名的话第一个字节表示域名的长度为n，紧接着n个字节表示域名
                is.read(buff, 0, 1);
                int domainLength = buff[0];
                is.read(buff, 0, domainLength);
                targetAddress = new String(Arrays.copyOfRange(buff, 0, domainLength));
                break;
            case IPV4:
                // 如果是ipv4的话使用固定的4个字节表示地址
                is.read(buff, 0, 4);
                targetAddress = ipAddressBytesToString(buff);
                break;
            case IPV6:
                throw new RuntimeException("not support ipv6.");
            default:
                throw new RuntimeException("addressType error!");
        }

        is.read(buff, 0, 2);
        int targetPort = ((buff[0] & 0XFF) << 8) | (buff[1] & 0XFF);

        StringBuilder msg = new StringBuilder();
        msg.append("version=").append(version).append(", cmd=").append(command.name())
                .append(", addressType=").append(addressType.name())
                .append(", domain=").append(targetAddress).append(", port=").append(targetPort);
        handleLog(msg.toString());

        // 响应客户端发送的命令，暂时只实现CONNECT命令
        switch (command) {
            case CONNECT:
                handleConnectCommand(targetAddress, targetPort);
                break;
            case BIND:
                throw new RuntimeException("not support command BIND");
            case UDP_ASSOCIATE:
                throw new RuntimeException("not support command UDP_ASSOCIATE");
            default:
                throw new RuntimeException("command type is error.");
        }

    }

    /**
     * convert ip address from 4 byte to string
     *
     * @param ipAddressBytes
     * @return
     */
    private String ipAddressBytesToString(byte[] ipAddressBytes) {
        // first convert to int avoid negative
        return (ipAddressBytes[0] & 0XFF) + "." + (ipAddressBytes[1] & 0XFF) + "." + (ipAddressBytes[2] & 0XFF) + "." + (ipAddressBytes[3] & 0XFF);
    }

    /**
     * 处理CONNECT命令
     *
     * @param targetAddress
     * @param targetPort
     * @throws IOException
     */
    private void handleConnectCommand(String targetAddress, int targetPort) throws IOException {
        Socket targetSocket = null;
        try {
            targetSocket = new Socket(targetAddress, targetPort);
        } catch (IOException e) {
            sendCommandResponse(CommandStatus.GENERAL_SOCKS_SERVER_FAILURE);
            return;
        }
        sendCommandResponse(CommandStatus.SUCCEEDED);
        new SocketForwarding(clientSocket, targetSocket).start();
    }

    private void sendCommandResponse(CommandStatus commandStatus) throws IOException {
        OutputStream os = clientSocket.getOutputStream();
        os.write(buildCommandResponse(commandStatus.getRangeStart()));
        os.flush();
    }

    private byte[] buildCommandResponse(byte commandStatusCode) {
        ByteBuffer payload = ByteBuffer.allocate(100);
        payload.put(VERSION);
        payload.put(commandStatusCode);
        payload.put(RSV);
//          payload.put(AddressType.IPV4.getValue());
//          payload.put(SERVER_IP_ADDRESS.getBytes());
        payload.put(AddressType.DOMAIN.getValue());
        byte[] addressBytes = SERVER_IP_ADDRESS.getBytes();
        payload.put((byte) addressBytes.length);
        payload.put(addressBytes);
        payload.put((byte) (((Socks5ProxyServer.SERVICE_LISTENER_PORT & 0XFF00) >> 8)));
        payload.put((byte) (Socks5ProxyServer.SERVICE_LISTENER_PORT & 0XFF));
        byte[] payloadBytes = new byte[payload.position()];
        payload.flip();
        payload.get(payloadBytes);
        return payloadBytes;
    }

    private void handleLog(String format, Object... args) {
        log("handle, clientIp=" + clientIp + ", port=" + clientPort + ", " + format, args);
    }

}
