package de.cyzetlc.smp.handler.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jspecify.annotations.NonNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryHandler implements IMySQLExtension {
    private HikariDataSource hikari;

    public QueryHandler(IDatabaseCredentials credentials) {
        HikariConfig config = getHikariConfig(credentials);

        try {
            this.hikari = new HikariDataSource(config);
            Connection connection = this.getNewConnection();
            String timeZone = null;
            ResultSet databaseTimeZone = connection.prepareStatement("SELECT @@GLOBAL.time_zone AS time_zone;").executeQuery();

            if (databaseTimeZone.next()) {
                timeZone = databaseTimeZone.getString("time_zone");
            }

            databaseTimeZone.close();
            connection.close();
            this.closeConnection(connection);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    private static @NonNull HikariConfig getHikariConfig(IDatabaseCredentials credentials) {
        HikariConfig config = new HikariConfig();
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());
        config.setConnectionTimeout(5000L);
        config.setMaximumPoolSize(credentials.getPoolSize());

        String jdbcConStr = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&verifyServerCertificate=false&allowPublicKeyRetrieval=true&characterEncoding=latin1",
                credentials.getHostname(), credentials.getPort(), credentials.getDatabase());

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcConStr);
        return config;
    }

    public MySQLQueryBuilder createBuilder(String qry) {
        return new MySQLQueryBuilder(this).setQuery(qry);
    }

    public MySQLQueryBuilder createBuilder() {
        return new MySQLQueryBuilder(this);
    }

    @Override
    public Connection getNewConnection() {
        try {
            return this.hikari.getConnection();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
            return null;
        }
    }

    @Override
    public void closeConnection(Connection connection) {
        this.hikari.evictConnection(connection);
    }

    @Override
    public void stop() {
        if (!this.hikari.isClosed()) {
            this.hikari.close();
        }
    }
}
