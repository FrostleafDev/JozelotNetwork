package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooterPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Collection;
import java.util.List;

public class ScoreboardPacketSender {

    /**
     * Sendet ein Team-Paket an einen Spieler, um die Sortierung zu steuern.
     * @param viewer Der Spieler, der das Paket empfängt (dessen Tabliste)
     * @param playerName Der Name des Spielers, der sortiert werden soll
     * @param teamName Der Teamname (z.B. "001Owner") - bestimmt die Sortierung!
     */
    public static void sendTeamPacket(Player viewer, String playerName, String teamName) {
        ConnectedPlayer connectedPlayer = (ConnectedPlayer) viewer;

        // Modus 0 = Erstellen, Modus 3 = Spieler hinzufügen, Modus 4 = Spieler entfernen
        // Wir nutzen hier der Einfachheit halber immer "Erstellen" (0) für jeden Eintrag

        ByteBuf buf = Unpooled.buffer();
        try {
            ProtocolUtils.writeString(buf, teamName); // Team Name
            buf.writeByte(0); // Mode: Create Team

            ProtocolUtils.writeComponent(buf, "{\"text\":\"\"}"); // Display Name (leer)
            buf.writeByte(0); // Friendly flags
            ProtocolUtils.writeString(buf, "always"); // Name tag visibility
            ProtocolUtils.writeString(buf, "always"); // Collision rule
            buf.writeInt(15); // Color (Reset/White)
            ProtocolUtils.writeComponent(buf, "{\"text\":\"\"}"); // Prefix (leer)
            ProtocolUtils.writeComponent(buf, "{\"text\":\"\"}"); // Suffix (leer)

            // Spieler zum Team hinzufügen
            buf.writeBoolean(true); // Has entities?
            ProtocolUtils.writeVarInt(buf, 1); // Entity Count
            ProtocolUtils.writeString(buf, playerName); // Der Spielername

            // Paket senden (Paket ID für Teams ist 0x56 oder ähnlich, je nach Version)
            // HINWEIS: Da IDs sich ändern, ist die stabilste Methode über eine Bridge.
            // Aber hier ist der manuelle Weg:
            connectedPlayer.getConnection().write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}