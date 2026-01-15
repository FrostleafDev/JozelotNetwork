package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import redis.clients.jedis.JedisPooled;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLSetup {

    private final ConfigManager config;
    private final ConsoleLogger consoleLogger;
    private Connection connection;

    private final String url;
    private final String user;
    private final String password;

    public MySQLSetup(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.consoleLogger = plugin.getConsoleLogger();

        url = "jdbc:mariadb://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase();
        user = config.getMysqlUser();
        password = config.getMysqlPassword();
    }

    public void setup() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");

            this.connection = DriverManager.getConnection(url, user, password);
            consoleLogger.broadCastToConsole("MariaDB: Leitung steht.");

            consoleLogger.broadCastToConsole("MariaDB: Tabellen erfolgreich initialisiert.");

        } catch (SQLException e) {
            consoleLogger.broadCastToConsole("MariaDB: Setup fehlgeschlagen: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            consoleLogger.broadCastToConsole("MariaDB: Treiber nicht gefunden!");
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                setup();
            }
        } catch (SQLException e) {
            setup();
        }
        return connection;
    }
}
