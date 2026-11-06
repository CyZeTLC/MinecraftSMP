package de.cyzetlc.smp.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Location loc = event.getEntity().getLocation();

        if (loc.distance(event.getEntity().getWorld().getSpawnLocation()) < 50) {
            if (event.getDamager() instanceof Player damager) {
                if (!damager.hasPermission("smp.spawn.damage_entities")) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
