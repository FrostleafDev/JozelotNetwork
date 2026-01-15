package de.jozelot.jozelotProxy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.jozelot.jozelotProxy.JozelotProxy;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLSetup {

    private HikariDataSource dataSource;
    private final String url;
    private final String user;
    private final String password;

    public MySQLSetup(JozelotProxy plugin) {
        var config = plugin.getConfig();
        this.url = "jdbc:mariadb://" + config.getMysqlHost() + ":" + config.getMysqlPort() + "/" + config.getMysqlDatabase();
        this.user = config.getMysqlUser();
        this.password = config.getMysqlPassword();
    }

    public void setup() {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.setConnectionTimeout(5000);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            setup();
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}