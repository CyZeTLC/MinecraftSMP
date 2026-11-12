package de.cyzetlc.smp.listener;

import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PortalListener implements Listener {
    private MinecraftSMP smp = MinecraftSMP.getInstance();

    @EventHandler
    public void onEndPortal(PlayerPortalEvent event) {
        if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
            if (!smp.isEndIsEnabled()) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(
                        smp.getMessageHandler().getMessageForUUID(
                                event.getPlayer().getUniqueId(),
                                "smp.portal.end.disabled"
                        )
                );
            }
        }
    }
}
