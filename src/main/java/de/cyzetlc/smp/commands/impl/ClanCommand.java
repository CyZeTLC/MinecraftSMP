package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import de.cyzetlc.smp.handler.clan.ClanHandler;
import de.cyzetlc.smp.model.Clan;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@CommandSpecification(
        command = "clan"
)
public class ClanCommand extends BaseCommand {
    private final ClanHandler clanHandler = MinecraftSMP.getInstance().getClanHandler();

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length == 0) {
            this.sendMessage(sender, "smp.command.clan.usage");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> this.create(p, args);
            case "invite" -> this.invite(p, args);
            case "accept" -> this.accept(p, args);
            case "kick" -> this.kick(p, args);
            case "info" -> this.info(p);
            case "leave" -> this.leave(p);
            case "delete" -> this.delete(p);
            default -> this.sendMessage(sender, "smp.command.clan.usage");
        }
    }

    private void create(Player p, String[] args) {
        if (args.length != 3) {
            this.sendMessage(p, "smp.command.clan.create.usage");
            return;
        }

        if (this.clanHandler.getClanOfPlayer(p.getUniqueId()) != null) {
            this.sendMessage(p, "smp.command.clan.already_in_clan");
            return;
        }

        String name = args[1];
        String tag = args[2];

        if (!name.matches("[A-Za-z0-9_]{3,16}")) {
            this.sendMessage(p, "smp.command.clan.create.invalid_name");
            return;
        }

        if (!tag.matches("[A-Za-z0-9]{2,4}")) {
            this.sendMessage(p, "smp.command.clan.create.invalid_tag");
            return;
        }

        if (this.clanHandler.getClanByName(name) != null) {
            this.sendMessage(p, "smp.command.clan.create.name_taken", name);
            return;
        }

        if (this.clanHandler.getClanByTag(tag) != null) {
            this.sendMessage(p, "smp.command.clan.create.tag_taken", tag);
            return;
        }

        Clan clan = this.clanHandler.createClan(name, tag, p.getUniqueId());
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        this.sendMessage(p, "smp.command.clan.create.success", clan.getName(), clan.getTag());
    }

    private void invite(Player p, String[] args) {
        if (args.length != 2) {
            this.sendMessage(p, "smp.command.clan.invite.usage");
            return;
        }

        Clan clan = this.clanHandler.getClanOfPlayer(p.getUniqueId());

        if (clan == null) {
            this.sendMessage(p, "smp.command.clan.not_in_clan");
            return;
        }

        if (!clan.getOwner().equals(p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.not_owner");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);

        if (target == null) {
            this.sendMessage(p, "smp.command.clan.player_not_found", args[1]);
            return;
        }

        if (target.equals(p)) {
            this.sendMessage(p, "smp.command.clan.invite.self");
            return;
        }

        if (this.clanHandler.getClanOfPlayer(target.getUniqueId()) != null) {
            this.sendMessage(p, "smp.command.clan.invite.target_in_clan", target.getName());
            return;
        }

        if (!this.clanHandler.invite(clan, target.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.invite.already_invited", target.getName());
            return;
        }

        this.sendMessage(p, "smp.command.clan.invite.sent", target.getName());
        target.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getMessageForUUID(target.getUniqueId(), "smp.command.clan.invite.received",
                p.getName(), clan.getName(), clan.getTag()));
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }

    private void accept(Player p, String[] args) {
        if (args.length != 2) {
            this.sendMessage(p, "smp.command.clan.accept.usage");
            return;
        }

        if (this.clanHandler.getClanOfPlayer(p.getUniqueId()) != null) {
            this.sendMessage(p, "smp.command.clan.already_in_clan");
            return;
        }

        Clan clan = this.clanHandler.getClanByTag(args[1]);

        if (clan == null || !this.clanHandler.hasInvite(clan, p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.accept.no_invite", args[1]);
            return;
        }

        this.clanHandler.sendMessage(clan, "smp.command.clan.accept.joined", p.getName());
        this.clanHandler.addMember(clan, p.getUniqueId());
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        this.sendMessage(p, "smp.command.clan.accept.success", clan.getName());
    }

    private void kick(Player p, String[] args) {
        if (args.length != 2) {
            this.sendMessage(p, "smp.command.clan.kick.usage");
            return;
        }

        Clan clan = this.clanHandler.getClanOfPlayer(p.getUniqueId());

        if (clan == null) {
            this.sendMessage(p, "smp.command.clan.not_in_clan");
            return;
        }

        if (!clan.getOwner().equals(p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.not_owner");
            return;
        }

        UUID target = this.getMember(clan, args[1]);

        if (target == null) {
            this.sendMessage(p, "smp.command.clan.kick.not_member", args[1]);
            return;
        }

        if (target.equals(p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.kick.self");
            return;
        }

        String targetName = this.getName(target);
        this.clanHandler.removeMember(clan, target);
        this.clanHandler.sendMessage(clan, "smp.command.clan.kick.success", targetName);

        Player targetPlayer = Bukkit.getPlayer(target);

        if (targetPlayer != null) {
            targetPlayer.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getMessageForUUID(target, "smp.command.clan.kick.target", clan.getName()));
        }
    }

    private void info(Player p) {
        Clan clan = this.clanHandler.getClanOfPlayer(p.getUniqueId());

        if (clan == null) {
            this.sendMessage(p, "smp.command.clan.not_in_clan");
            return;
        }

        List<String> members = new ArrayList<>();

        for (UUID uuid : clan.getMembers()) {
            members.add((Bukkit.getPlayer(uuid) != null ? "§a" : "§7") + this.getName(uuid));
        }

        this.sendMessage(p, "smp.command.clan.info.separator");
        this.sendMessage(p, "smp.command.clan.info.name", clan.getName());
        this.sendMessage(p, "smp.command.clan.info.tag", clan.getTag());
        this.sendMessage(p, "smp.command.clan.info.owner", this.getName(clan.getOwner()));
        this.sendMessage(p, "smp.command.clan.info.created", new SimpleDateFormat("dd.MM.yyyy").format(new Date(clan.getCreated())));
        this.sendMessage(p, "smp.command.clan.info.members", String.valueOf(members.size()), String.join("§8, ", members));
        this.sendMessage(p, "smp.command.clan.info.separator");
    }

    private void leave(Player p) {
        Clan clan = this.clanHandler.getClanOfPlayer(p.getUniqueId());

        if (clan == null) {
            this.sendMessage(p, "smp.command.clan.not_in_clan");
            return;
        }

        if (clan.getOwner().equals(p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.leave.owner");
            return;
        }

        this.clanHandler.removeMember(clan, p.getUniqueId());
        this.clanHandler.sendMessage(clan, "smp.command.clan.leave.broadcast", p.getName());
        this.sendMessage(p, "smp.command.clan.leave.success", clan.getName());
    }

    private void delete(Player p) {
        Clan clan = this.clanHandler.getClanOfPlayer(p.getUniqueId());

        if (clan == null) {
            this.sendMessage(p, "smp.command.clan.not_in_clan");
            return;
        }

        if (!clan.getOwner().equals(p.getUniqueId())) {
            this.sendMessage(p, "smp.command.clan.not_owner");
            return;
        }

        this.clanHandler.sendMessage(clan, "smp.command.clan.delete.success", clan.getName());
        this.clanHandler.deleteClan(clan);
    }

    private UUID getMember(Clan clan, String name) {
        for (UUID uuid : clan.getMembers()) {
            if (this.getName(uuid).equalsIgnoreCase(name)) {
                return uuid;
            }
        }
        return null;
    }

    private String getName(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        return name != null ? name : "Unbekannt";
    }
}
