package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import redis.clients.jedis.JedisPooled;

public class MySQLSetup {

    private final ConfigManager config;
    private final ConsoleLogger consoleLogger;
    private JedisPooled jedis;

    public MySQLSetup(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    public void setup() {
        String host = config.getMysqlHost();
        String database = config.getMysqlDatabase();
        String user = config.getMysqlUser();
        String password = config.getMysqlPassword();
        int port = config.getMysqlPort();


    }
}
