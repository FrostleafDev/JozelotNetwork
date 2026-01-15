package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.database.MySQLManager;

public class DisconnectListener {

    private final MySQLManager mySQLManager;

    public DisconnectListener(JozelotProxy plugin) {
        this.mySQLManager = plugin.getMySQLManager();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {

    }
}
