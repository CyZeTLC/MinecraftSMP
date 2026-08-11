package de.cyzetlc.smp.handler.database;

import java.sql.Connection;

public interface IMySQLExtension {
    /**
     * @return new generated connection
     */
    Connection getNewConnection();

    /**
     * @param connection the connection to close
     */
    void closeConnection(Connection connection);

    /**
     * Stops database connection
     */
    void stop();
}
