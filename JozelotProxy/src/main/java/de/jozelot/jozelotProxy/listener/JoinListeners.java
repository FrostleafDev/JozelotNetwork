package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.database.MySQLManager;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class JoinListeners {

    MiniMessage mm = MiniMessage.miniMessage();
    private final ConsoleLogger consoleLogger;
    private final MySQLManager mySQLManager;

    public JoinListeners(JozelotProxy plugin) {
        this.consoleLogger = plugin.getConsoleLogger();
        this.mySQLManager = plugin.getMySQLSetup().get;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        //player.sendMessage(mm.deserialize("Willkommen :)"));
    }
}
