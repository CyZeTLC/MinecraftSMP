package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandSpecification(
        command = "end",
        cooldownValue = 0,
        cooldownType = CommandSpecification.TimeUnit.SECONDS,
        permission = "smp.command.end"
)
public class EndCommand extends BaseCommand {
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        boolean endEnabled = MinecraftSMP.getInstance().isEndIsEnabled();

        MinecraftSMP.getInstance().setEndIsEnabled(!endEnabled);
        this.sendMessage(p, "smp.command.end.change", endEnabled ? "§aon" : "§coff");
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }
}
