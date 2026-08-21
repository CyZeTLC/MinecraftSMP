package de.cyzetlc.smp.listener;

import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
    private final MinecraftSMP minecraftSMP;

    public PlayerConnectionListener(MinecraftSMP minecraftSMP) {
        this.minecraftSMP = minecraftSMP;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(minecraftSMP.getMessageHandler().getMessageForUUID(player.getUniqueId(), "smp.join_message", event.getPlayer().getName())));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(minecraftSMP.getMessageHandler().getMessageForUUID(player.getUniqueId(), "smp.quit_message", event.getPlayer().getName())));
    }
}
