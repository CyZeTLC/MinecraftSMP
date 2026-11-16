package de.cyzetlc.smp.listener;

import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ClanListener implements Listener {
    private final MinecraftSMP minecraftSMP;

    public ClanListener(MinecraftSMP minecraftSMP) {
        this.minecraftSMP = minecraftSMP;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        minecraftSMP.getClanHandler().updatePlayer(event.getPlayer());
    }
}
