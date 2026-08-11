package de.cyzetlc.smp.service;

import java.util.UUID;

public interface ISystemPlayer {
    /**
     * @return the name of player
     */
    String getName();

    /**
     * @return the uuid of player
     */
    UUID getUUID();

    /**
     * @return the first login of this player
     */
    long getFirstLogin();

    /**
     * @return the last login of player
     */
    long getLastLogin();

    /**
     * @return the playtime of player
     */
    long getPlayTime();

    /**
     * @return the player online status
     */
    boolean isOnline();

    /**
     * @return the languageKey of player
     */
    String getLanguageKey();

    /**
     * @return the coins of player
     */
    int getCoins();

    /**
     * @param key sets the language key
     */
    void setLanguage(String key);

    /**
     * @param amount the amount to add to player's account
     */
    void addCoins(int amount);

    /**
     * @param amount the amount to remove of player's account
     */
    void removeCoins(int amount);

    /**
     * @return the current server of player
     */
    String getServer();
}
