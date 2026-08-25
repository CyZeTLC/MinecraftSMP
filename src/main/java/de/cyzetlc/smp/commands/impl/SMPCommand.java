package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.command.CommandSender;

@CommandSpecification(
        command = "smp",
        cooldownValue = 5,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class SMPCommand extends BaseCommand {
    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {

    }
}
