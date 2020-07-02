package com.alan.socketproxy.enums;

/**
 * @author chenxinhui
 * @Description: 客户端命令
 * @date 2020/7/2
 */
public enum Command {
    CONNECT((byte) 0X01, "CONNECT"),
    BIND((byte) 0X02, "BIND"),
    UDP_ASSOCIATE((byte) 0X03, "UDP ASSOCIATE");

    private byte value;
    private String description;

    Command(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Command convertToCmd(byte value) {
        for (Command cmd : Command.values()) {
            if (cmd.value == value) {
                return cmd;
            }
        }
        return null;
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
