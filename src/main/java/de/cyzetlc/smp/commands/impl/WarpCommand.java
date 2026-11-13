package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

@CommandSpecification(
        command = "warp",
        cooldownValue = 20,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class WarpCommand extends BaseCommand {
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length == 0) {
            this.sendMessage(sender, "smp.command.warp.usage");
            return;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") && p.hasPermission("smp.command.warp.set")) {
                boolean setWarp = this.setWarp(p.getLocation(), args[0]);

                if (setWarp) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    this.sendMessage(sender, "smp.command.warp.set.success", args[0]);
                } else {
                    p.playSound(p.getLocation(), Sound.ENTITY_ALLAY_DEATH, 1, 1);
                    this.sendMessage(sender, "smp.command.warp.set.failed", args[0]);
                }
                return;
            }
        }

        String warpName = args[0];
        Location warpLocation = this.getWarpByName(warpName);

        if (warpLocation == null) {
            this.sendMessage(sender, "smp.command.warp.not_found", warpName);
            return;
        }

        p.teleport(warpLocation);
        p.playSound(p.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        this.sendMessage(sender, "smp.command.warp.teleport", warpName);
    }

    private boolean setWarp(Location location, String name) {
        return false;
    }

    private Location getWarpByName(String name) {
        try (CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "SELECT x,y,z,yaw,pitch,world FROM warps WHERE name = ?"
        ).addParameter(name.toLowerCase()).executeQuerySync()) {
            float x = rs.getFloat("x");
            float y = rs.getFloat("y");
            float z = rs.getFloat("z");
            float yaw = rs.getFloat("yaw");
            float pitch = rs.getFloat("pitch");
            String worldName = rs.getString("world");

            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (SQLException e) {
            return null;
        }
    }
}
