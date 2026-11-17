package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@CommandSpecification(
        command = "smp",
        cooldownValue = 5,
        cooldownType = CommandSpecification.TimeUnit.SECONDS,
        permission = "smp.command.smp"
)
public class SMPCommand extends BaseCommand {
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                MinecraftSMP.getInstance().getMessageHandler().reload(bool -> {
                    if (bool) {
                        sender.sendMessage(ChatColor.GREEN + "Reload complete.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Reload failed.");
                    }
                });
            }
        }
    }
}
