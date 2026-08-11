package de.cyzetlc.smp.handler.database;

public interface IDatabaseCredentials {
    /**
     * @return the username to login
     */
    String getUsername();

    /**
     * @return the password to login
     */
    String getPassword();

    /**
     * @return the database host
     */
    String getHostname();

    /**
     * @return the database schema
     */
    String getDatabase();

    /**
     * @return the database port
     */
    int getPort();

    /**
     * @return the poolSize
     */
    int getPoolSize();
}
