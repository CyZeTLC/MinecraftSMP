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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CommandSpecification(
        command = "warp",
        cooldownValue = 5,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class WarpCommand extends BaseCommand {

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length == 0) {
            this.sendMessage(sender, "smp.command.warp.usage");
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Bukkit.getScheduler().runTaskAsynchronously(MinecraftSMP.getInstance(), () -> {
                List<String> warps = this.getAllWarps();

                Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                    if (warps.isEmpty()) {
                        this.sendMessage(sender, "smp.command.warp.list.empty");
                        return;
                    }

                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    this.sendMessage(sender, "smp.command.warp.list", String.valueOf(warps.size()), String.join(", ", warps));
                });
            });
            return;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") && p.hasPermission("smp.command.warp.set")) {
                boolean setWarp = this.setWarp(p.getLocation(), args[1]); // Fixed from args[0] to args[1]

                if (setWarp) {
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    this.sendMessage(sender, "smp.command.warp.set.success", args[1]);
                } else {
                    p.playSound(p.getLocation(), Sound.ENTITY_ALLAY_DEATH, 1, 1);
                    this.sendMessage(sender, "smp.command.warp.set.failed", args[1]);
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
        if (location.getWorld() == null) return false;

        MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "INSERT INTO warps (name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), "
                        + "z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)"
        ).addParameters(Arrays.asList(
                name.toLowerCase(), location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch()
        )).executeUpdateAsync();

        return true;
    }

    private Location getWarpByName(String name) {
        try (CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "SELECT x,y,z,yaw,pitch,world FROM warps WHERE name = ?"
        ).addParameter(name.toLowerCase()).executeQuerySync()) {

            if (!rs.next()) {
                return null;
            }

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

    private List<String> getAllWarps() {
        List<String> warps = new ArrayList<>();
        try (CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "SELECT name FROM warps ORDER BY name ASC"
        ).executeQuerySync()) {
            while (rs.next()) {
                warps.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warps;
    }
}