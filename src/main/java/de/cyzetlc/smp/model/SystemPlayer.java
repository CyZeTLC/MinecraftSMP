package de.cyzetlc.smp.model;

import de.cyzetlc.smp.service.ISystemPlayer;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class SystemPlayer implements ISystemPlayer {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public long getFirstLogin() {
        return 0;
    }

    @Override
    public long getLastLogin() {
        return 0;
    }

    @Override
    public long getPlayTime() {
        return 0;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getLanguageKey() {
        return "de";
    }

    @Override
    public void setLanguage(String key) {

    }

    @Override
    public int getCoins() {
        return 0;
    }

    @Override
    public void addCoins(int amount) {

    }

    @Override
    public void removeCoins(int amount) {

    }

    @Override
    public String getServer() {
        return null;
    }
}
