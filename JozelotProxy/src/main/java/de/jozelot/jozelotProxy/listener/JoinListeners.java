package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.database.MySQLManager;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JoinListeners {

    MiniMessage mm = MiniMessage.miniMessage();
    private final ConsoleLogger consoleLogger;
    private final MySQLManager mySQLManager;
    private final LangManager lang;
    private final JozelotProxy plugin;

    public JoinListeners(JozelotProxy plugin) {
        this.consoleLogger = plugin.getConsoleLogger();
        this.mySQLManager = plugin.getMySQLManager();
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();

        plugin.getServer().getScheduler()
                .buildTask(plugin, () -> {

                    boolean isNew = mySQLManager.addToPlayerList(uuid, username);

                    if (isNew) {
                        List<String> welcome = lang.formatList("first-join", Map.of("player-name", username));
                        for (String line : welcome) {
                            player.sendMessage(mm.deserialize(line));
                        }
                    }

                })
                .schedule();
    }
}
