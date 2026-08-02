package de.jozelot.jozelotLobby.ui.lobbyHeads;

import de.jozelot.jozelotLobby.JozelotLobby;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class LobbyHeadManager {

    private final JozelotLobby plugin;
    private final List<LobbyHead> heads = new ArrayList<>();

    public LobbyHeadManager(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public void register() {
        heads.add(new LobbyHead(2, 66, 3,
                "<newline><gray>➥ <#9146FF><bold>Twitch </bold><#CCCCCC>» <click:open_url:'https://jzlt.de/twitch'><hover:show_text:'<#CCCCCC>Klicke um <white>Twitch</white> zu öffnen'><u>jzlt.de/twitch</u></hover></click><newline>"
        ));
        heads.add(new LobbyHead(3, 66, 2,
                "<newline><gray>➥ <#FF0000><bold>Youtube </bold><#CCCCCC>» <click:open_url:'https://jzlt.de/yt'><hover:show_text:'<#CCCCCC>Klicke um <white>Youtube</white> zu öffnen'><u>jzlt.de/yt</u></hover></click><newline>"
        ));
        heads.add(new LobbyHead(-3, 66, -2,
                "<newline><gray>➥ <#55FFFF><bold>Webseite </bold><#CCCCCC>» <click:open_url:'https://jozelot.de'><hover:show_text:'<#CCCCCC>Klicke um <white>jozelot.de</white> zu öffnen'><u>jozelot.de</u></hover></click><newline>"
        ));
        heads.add(new LobbyHead(-2, 66, -3,
                "<newline><gray>➥ <#5865F2><bold>Discord </bold><#CCCCCC>» <click:open_url:'https://jzlt.de/dc'><hover:show_text:'<#CCCCCC>Klicke um <white>Discord</white> zu öffnen'><u>jzlt.de/dc</u></hover></click><newline>"
        ));
    }

    public LobbyHead getHeadAt(Location loc) {
        for (LobbyHead head : heads) {
            if (head.isAt(loc)) {
                return head;
            }
        }
        return null;
    }
}