/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.audiodevice.impl.standard.tcp;

import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;

import java.util.Objects;

/**
 * @author zilong
 * @date 2022/4/14
 */
public class InboundMessage extends BaseMessage implements Msg.Inbound {

    private final Packet packet;

    private final String deviceId;

    public InboundMessage(Packet packet) {
        this.packet = packet;
        byte[] bytes = packet.getDeviceId();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(String.format("%02X", aByte));
        }
        deviceId = sb.toString();
    }

    public Packet getPacket() {
        return packet;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        InboundMessage that = (InboundMessage) o;
        return Objects.equals(packet, that.packet) && Objects.equals(deviceId, that.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), packet, deviceId);
    }

    @Override
    public String toString() {
        return "InboundMessage{" +
                "packet=" + packet +
                ", deviceId='" + deviceId + '\'' +
                "} " + super.toString();
    }
}
