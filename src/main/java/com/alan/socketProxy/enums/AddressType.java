package com.alan.socketproxy.enums;

/**
 * @author chenxinhui
 * @Description: 请求的地址类型
 * @date 2020/7/2
 */
public enum AddressType {
    IPV4((byte) 0X01, "the address is a version-4 IP address, with a length of 4 octets"),
    DOMAIN((byte) 0X03, "the address field contains a fully-qualified domain name.  The first\n" +
            "   octet of the address field contains the number of octets of name that\n" +
            "   follow, there is no terminating NUL octet."),
    IPV6((byte) 0X04, "the address is a version-6 IP address, with a length of 16 octets.");
    private byte value;
    private String description;

    AddressType(byte value, String description) {
        this.value = value;
        this.description = description;
    }

    public static AddressType convertToAddressType(byte value) {
        for (AddressType addressType : AddressType.values()) {
            if (addressType.value == value) {
                return addressType;
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
