package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

@CommandSpecification(
        command = "rtp",
        cooldownValue = 60,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class RTPCommand extends BaseCommand {
    private final Random random = new Random();
    private final int MIN_DISTANCE;
    private final int MAX_DISTANCE;

    public RTPCommand(MinecraftSMP smp) {
        this.MIN_DISTANCE = smp.getJsonConfig().getObject().get("rtpMinDistance").getAsInt();
        this.MAX_DISTANCE = smp.getJsonConfig().getObject().get("rtpMaxDistance").getAsInt();
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        World world = p.getWorld();

        if (world.getEnvironment() != World.Environment.NORMAL) {
            this.sendMessage(sender, "smp.command.rtp.only-overworld");
            return;
        }

        if (!isOnGround(p)) {
            this.sendMessage(sender, "smp.command.rtp.must-be-on-ground");
            return;
        }

        this.sendMessage(sender, "smp.command.rtp.searching");

        Location safeLocation = findSafeLocation(world);

        if (safeLocation == null) {
            this.sendMessage(sender, "smp.command.rtp.failed");
            return;
        }

        p.teleport(safeLocation);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

        this.sendMessage(sender, "smp.command.rtp.success",
                String.valueOf(safeLocation.getBlockX()),
                String.valueOf(safeLocation.getBlockZ()));
    }

    private Location findSafeLocation(World world) {
        int attempts = 0;

        while (attempts < 10) {
            attempts++;

            int x = random.nextInt(MAX_DISTANCE * 2) - MAX_DISTANCE;
            int z = random.nextInt(MAX_DISTANCE * 2) - MAX_DISTANCE;

            if (Math.abs(x) < MIN_DISTANCE && Math.abs(z) < MIN_DISTANCE) {
                continue;
            }

            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            Block block = loc.getBlock();
            Block below = world.getBlockAt(x, y - 1, z);

            if (below.getType().isSolid() && !isDangerous(block.getType())) {
                return loc;
            }
        }

        return null;
    }

    private boolean isDangerous(org.bukkit.Material material) {
        String name = material.name();
        return name.contains("LAVA") || name.contains("FIRE") || name.contains("CACTUS") || name.contains("WATER");
    }

    public boolean isOnGround(Player player) {
        org.bukkit.util.RayTraceResult result = player.getLocation().getWorld().rayTraceBlocks(
                player.getLocation().clone().add(0, -0.05, 0),
                new org.bukkit.util.Vector(0, -1, 0),
                0.1
        );

        return result != null && result.getHitBlock() != null;
    }
}
