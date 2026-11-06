package de.cyzetlc.smp.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (loc.distance(event.getBlock().getWorld().getSpawnLocation()) < 50
                && !event.getPlayer().hasPermission("smp.spawn.break_blocks")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();

        if (loc.distance(event.getBlock().getWorld().getSpawnLocation()) < 50
                && !event.getPlayer().hasPermission("smp.spawn.place_blocks"))  {
            event.setCancelled(true);
        }
    }
}
