package de.cyzetlc.smp.listener;

import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    private final MinecraftSMP minecraftSMP;

    public PlayerChatListener(MinecraftSMP minecraftSMP) {
        this.minecraftSMP = minecraftSMP;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        event.setCancelled(true);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(minecraftSMP.getMessageHandler().getStaticMessage("smp.chat_format", player.getDisplayName(), message)));
    }
}
