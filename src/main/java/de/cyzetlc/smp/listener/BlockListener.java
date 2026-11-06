package de.cyzetlc.smp.listener;

import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    private final int SPAWN_RADIUS;

    public BlockListener(MinecraftSMP smp) {
        this.SPAWN_RADIUS = smp.getJsonConfig().getObject().get("spawnRadius").getAsInt();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (loc.distance(event.getBlock().getWorld().getSpawnLocation()) < this.SPAWN_RADIUS
                && !event.getPlayer().hasPermission("smp.spawn.break_blocks")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();

        if (loc.distance(event.getBlock().getWorld().getSpawnLocation()) < this.SPAWN_RADIUS
                && !event.getPlayer().hasPermission("smp.spawn.place_blocks"))  {
            event.setCancelled(true);
        }
    }
}
