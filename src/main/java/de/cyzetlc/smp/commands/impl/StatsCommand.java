package de.cyzetlc.smp.commands.impl;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import de.cyzetlc.smp.commands.BaseCommand;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

@CommandSpecification(
        command = "stats",
        cooldownValue = 10,
        cooldownType = CommandSpecification.TimeUnit.SECONDS
)
public class StatsCommand extends BaseCommand {
    private static final int TOP_LIMIT = 10;

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player p)) return;

        if (args.length > 1) {
            this.sendMessage(sender, "smp.command.stats.usage");
            return;
        }

        OfflinePlayer target = p;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("top")) {
                this.showTop(p);
                return;
            }

            target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                target = Bukkit.getOfflinePlayerIfCached(args[0]);
            }

            if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
                this.sendMessage(sender, "smp.command.stats.player_not_found", args[0]);
                return;
            }
        }

        this.showStats(p, target);
    }

    private void showTop(Player p) {
        List<OfflinePlayer> ranking = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(OfflinePlayer::hasPlayedBefore)
                .sorted(Comparator.comparingInt((OfflinePlayer o) -> o.getStatistic(Statistic.DAMAGE_DEALT)).reversed())
                .limit(TOP_LIMIT)
                .toList();

        this.sendMessage(p, "smp.command.stats.separator");
        this.sendMessage(p, "smp.command.stats.top.title", String.valueOf(TOP_LIMIT));

        for (int i = 0; i < ranking.size(); i++) {
            OfflinePlayer player = ranking.get(i);
            this.sendMessage(
                    p,
                    "smp.command.stats.top.entry",
                    String.valueOf(i + 1),
                    this.safe(player.getName()),
                    this.formatDamage(player.getStatistic(Statistic.DAMAGE_DEALT))
            );
        }

        this.sendMessage(p, "smp.command.stats.separator");
    }

    private void showStats(Player viewer, OfflinePlayer target) {
        String name = this.safe(target.getName());

        this.sendMessage(viewer, "smp.command.stats.separator");
        this.sendMessage(viewer, "smp.command.stats.title", name);
        this.sendMessage(viewer, "smp.command.stats.playtime", this.formatPlayTime(target.getStatistic(Statistic.PLAY_ONE_MINUTE)));
        this.sendMessage(viewer, "smp.command.stats.deaths", String.valueOf(target.getStatistic(Statistic.DEATHS)));
        this.sendMessage(viewer, "smp.command.stats.mob_kills", String.valueOf(target.getStatistic(Statistic.MOB_KILLS)));
        this.sendMessage(viewer, "smp.command.stats.player_kills", String.valueOf(target.getStatistic(Statistic.PLAYER_KILLS)));
        this.sendMessage(viewer, "smp.command.stats.damage_dealt", this.formatDamage(target.getStatistic(Statistic.DAMAGE_DEALT)));
        this.sendMessage(viewer, "smp.command.stats.damage_taken", this.formatDamage(target.getStatistic(Statistic.DAMAGE_TAKEN)));

        if (viewer.hasPermission("smp.command.stats.ban")) {
            this.showBan(viewer, name);
        }

        this.sendMessage(viewer, "smp.command.stats.separator");
    }

    private void showBan(Player viewer, String name) {
        BanList<?> banList = Bukkit.getBanList(BanList.Type.NAME);
        BanEntry<?> banEntry = banList.getBanEntry(name);

        if (banEntry == null) return;

        this.sendMessage(viewer, "smp.command.stats.separator");
        this.sendMessage(viewer, "smp.command.stats.ban.status");
        this.sendMessage(viewer, "smp.command.stats.ban.reason", this.safe(banEntry.getReason()));
        this.sendMessage(viewer, "smp.command.stats.ban.source", this.safe(banEntry.getSource()));
        this.sendMessage(viewer, "smp.command.stats.ban.created", this.formatDate(banEntry.getCreated()));

        if (banEntry.getExpiration() != null) {
            this.sendMessage(viewer, "smp.command.stats.ban.expires", this.formatDate(banEntry.getExpiration()));
        } else {
            this.sendMessage(viewer, "smp.command.stats.ban.permanent");
        }
    }

    private String formatPlayTime(long ticks) {
        long minutes = ticks / 20 / 60;
        return String.format("%dd %dh %dm", minutes / 1440, minutes % 1440 / 60, minutes % 60);
    }

    private String formatDamage(int value) {
        return String.format(Locale.GERMANY, "%.1f", value / 20.0);
    }

    private String formatDate(Date date) {
        if (date == null) return this.safe(null);

        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    private String safe(String value) {
        return value != null ? value : MinecraftSMP.getInstance().getMessageHandler().getStaticMessage("smp.command.stats.unknown");
    }
}