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
        command = "home",
        cooldownValue = 5,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class HomeCommand extends BaseCommand {

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length == 0) {
            this.sendMessage(sender, "smp.command.home.usage");
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            Bukkit.getScheduler().runTaskAsynchronously(MinecraftSMP.getInstance(), () -> {
                List<String> homes = this.getPlayerHomes(p.getUniqueId().toString());

                Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                    if (homes.isEmpty()) {
                        this.sendMessage(sender, "smp.command.home.list.empty");
                        return;
                    }

                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    this.sendMessage(sender, "smp.command.home.list", String.valueOf(homes.size()), String.join(", ", homes));
                });
            });
            return;
        }

        if (args.length == 2) {
            String subAction = args[0].toLowerCase();
            String homeName = args[1].toLowerCase();
            String uuid = p.getUniqueId().toString();

            if (subAction.equals("set")) {
                int maxHomes = MinecraftSMP.getInstance().getJsonConfig().getObject().get("maxHomes").getAsInt();

                Bukkit.getScheduler().runTaskAsynchronously(MinecraftSMP.getInstance(), () -> {
                    List<String> existingHomes = this.getPlayerHomes(uuid);
                    boolean isAlreadySet = existingHomes.contains(homeName);

                    if (!isAlreadySet && existingHomes.size() >= maxHomes) {
                        Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                            this.sendMessage(sender, "smp.command.home.max_reached", String.valueOf(maxHomes));
                        });
                        return;
                    }

                    boolean setHome = this.setHome(uuid, p.getLocation(), homeName);

                    Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                        if (setHome) {
                            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                            this.sendMessage(sender, "smp.command.home.set.success", homeName);
                        } else {
                            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);
                            this.sendMessage(sender, "smp.command.home.set.failed", homeName);
                        }
                    });
                });
                return;
            }

            if (subAction.equals("delete") || subAction.equals("del")) {
                Bukkit.getScheduler().runTaskAsynchronously(MinecraftSMP.getInstance(), () -> {
                    boolean deleted = this.deleteHome(uuid, homeName);

                    Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                        if (deleted) {
                            p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                            this.sendMessage(sender, "smp.command.home.delete.success", homeName);
                        } else {
                            p.playSound(p.getLocation(), Sound.ENTITY_ALLAY_DEATH, 1, 1);
                            this.sendMessage(sender, "smp.command.home.delete.not_found", homeName);
                        }
                    });
                });
                return;
            }
        }

        String homeName = args[0].toLowerCase();

        Bukkit.getScheduler().runTaskAsynchronously(MinecraftSMP.getInstance(), () -> {
            Location homeLocation = this.getHomeByName(p.getUniqueId().toString(), homeName);

            Bukkit.getScheduler().runTask(MinecraftSMP.getInstance(), () -> {
                if (homeLocation == null || homeLocation.getWorld() == null) {
                    this.sendMessage(sender, "smp.command.home.not_found", homeName);
                    return;
                }

                p.teleport(homeLocation);
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                this.sendMessage(sender, "smp.command.home.teleport", homeName);
            });
        });
    }

    private boolean deleteHome(String uuid, String name) {
        try {
            Location loc = this.getHomeByName(uuid, name);
            if (loc == null) return false;

            MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                    "DELETE FROM player_homes WHERE uuid = ? AND name = ?"
            ).addParameters(Arrays.asList(uuid, name)).executeUpdateAsync();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setHome(String uuid, Location location, String name) {
        if (location.getWorld() == null) return false;

        try {
            MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                    "INSERT INTO player_homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE world = VALUES(world), x = VALUES(x), y = VALUES(y), "
                            + "z = VALUES(z), yaw = VALUES(yaw), pitch = VALUES(pitch)"
            ).addParameters(Arrays.asList(
                    uuid, name, location.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ(),
                    location.getYaw(), location.getPitch()
            )).executeUpdateAsync();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Location getHomeByName(String uuid, String name) {
        try (CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "SELECT x, y, z, yaw, pitch, world FROM player_homes WHERE uuid = ? AND name = ?"
        ).addParameters(Arrays.asList(uuid, name)).executeQuerySync()) {

            if (!rs.next()) {
                return null;
            }

            double x = rs.getDouble("x");
            double y = rs.getDouble("y");
            double z = rs.getDouble("z");
            float yaw = rs.getFloat("yaw");
            float pitch = rs.getFloat("pitch");
            String worldName = rs.getString("world");

            if (Bukkit.getWorld(worldName) == null) {
                return null;
            }

            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (SQLException e) {
            return null;
        }
    }

    private List<String> getPlayerHomes(String uuid) {
        List<String> homes = new ArrayList<>();
        try (CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder(
                "SELECT name FROM player_homes WHERE uuid = ? ORDER BY name ASC"
        ).addParameter(uuid).executeQuerySync()) {
            while (rs.next()) {
                homes.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }
}
