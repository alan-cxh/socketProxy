package com.alan.socketproxy.enums;

/**
 * @author chenxinhui
 * @Description: 对于命令的处理结果
 * @date 2020/7/2
 */
public enum CommandStatus {
    SUCCEEDED((byte) 0X00, (byte) 0X00, "succeeded"),
    GENERAL_SOCKS_SERVER_FAILURE((byte) 0X01, (byte) 0X01, "general SOCKS server failure"),
    CONNECTION_NOT_ALLOWED_BY_RULESET((byte) 0X02, (byte) 0X02, "connection not allowed by ruleset"),
    NETWORK_UNREACHABLE((byte) 0X03, (byte) 0X03, "Network unreachable"),
    HOST_UNREACHABLE((byte) 0X04, (byte) 0X04, "Host unreachable"),
    CONNECTION_REFUSED((byte) 0X05, (byte) 0X05, "Connection refused"),
    TTL_EXPIRED((byte) 0X06, (byte) 0X06, "TTL expired"),
    COMMAND_NOT_SUPPORTED((byte) 0X07, (byte) 0X07, "Command not supported"),
    ADDRESS_TYPE_NOT_SUPPORTED((byte) 0X08, (byte) 0X08, "Address type not supported"),
    UNASSIGNED((byte) 0X09, (byte) 0XFF, "unassigned");

    private byte rangeStart;
    private byte rangeEnd;
    private String description;

    CommandStatus(byte rangeStart, byte rangeEnd, String description) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.description = description;
    }

    public byte getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(byte rangeStart) {
        this.rangeStart = rangeStart;
    }

    public byte getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(byte rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
