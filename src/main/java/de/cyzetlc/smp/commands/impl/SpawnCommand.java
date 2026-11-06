package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandSpecification(
        command = "spawn",
        cooldownValue = 10,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class SpawnCommand extends BaseCommand {
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        p.teleport(Bukkit.getWorlds().getFirst().getSpawnLocation());
        p.playSound(p.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        this.sendMessage(sender, "smp.command.spawn");
    }
}
