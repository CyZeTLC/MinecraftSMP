package de.cyzetlc.smp.handler.database;

import lombok.Getter;

@Getter
public class MySQLCredentials implements IDatabaseCredentials {
    public String username;
    public String password;
    public String hostname;
    public String database;
    public int port;
    public int poolSize;
}
