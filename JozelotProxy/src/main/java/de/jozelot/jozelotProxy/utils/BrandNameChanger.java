package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class BrandNameChanger {

    public void sendBrandName(Player player, String brand) {

        byte[] brandBytes = brand.getBytes(StandardCharsets.UTF_8);

        ByteBuf buf = Unpooled.buffer();
        try {
            writeVarInt(buf, brandBytes.length);
            buf.writeBytes(brandBytes);

            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);

            player.sendPluginMessage(() -> "minecraft:brand", data);
        } finally {
            buf.release();
        }
    }

    private void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

}
