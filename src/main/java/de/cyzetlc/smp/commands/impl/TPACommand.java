package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

@CommandSpecification(
        command = "tpa",
        cooldownValue = 10,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class TPACommand extends BaseCommand {
    private final HashMap<Player, Player> teleportRequests = new HashMap<>();

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length != 1) {
            this.sendMessage(sender, "smp.command.tpa.no-arguments");
            return;
        }

        if (args[0].equalsIgnoreCase("accept")) {
            Player target = this.teleportRequests.get(p);

            if (target == null) {
                this.sendMessage(sender, "smp.command.tpa.no-target");
                return;
            }

            target.teleport(p);
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            this.teleportRequests.remove(p);
            this.sendMessage(sender, "smp.command.tpa.success.target", target.getName());
        } else {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                this.sendMessage(sender, "smp.command.tpa.no-player", args[0]);
                return;
            }

            this.teleportRequests.put(p, target);
            this.sendMessage(sender, "smp.command.tpa.send.sender", target.getName());
            target.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getMessageForUUID(target.getUniqueId(), "smp.command.tpa.send.target"));
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1, 1);
        }
    }
}
