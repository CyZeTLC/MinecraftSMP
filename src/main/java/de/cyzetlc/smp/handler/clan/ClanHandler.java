package de.cyzetlc.smp.handler.clan;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.model.Clan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClanHandler {
    private static final String TEAM_PREFIX = "clan_";

    private final Map<UUID, Clan> clans = new LinkedHashMap<>();
    private final Map<UUID, Clan> playerClans = new HashMap<>();
    private final Map<UUID, Set<UUID>> invites = new HashMap<>();

    public void load() {
        this.execute("CREATE TABLE IF NOT EXISTS clans (id VARCHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16) NOT NULL UNIQUE, "
                + "tag VARCHAR(4) NOT NULL UNIQUE, owner VARCHAR(36) NOT NULL, created BIGINT NOT NULL)");
        this.execute("CREATE TABLE IF NOT EXISTS clan_members (uuid VARCHAR(36) NOT NULL PRIMARY KEY, clan_id VARCHAR(36) NOT NULL)");

        this.loadClans();
        this.loadMembers();
        this.rebuildTeams();
    }

    private void loadClans() {
        CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder("SELECT id, name, tag, owner, created FROM clans").executeQuerySync();

        if (rs == null) return;

        try {
            while (rs.next()) {
                Clan clan = new Clan(UUID.fromString(rs.getString("id")), rs.getString("name"), rs.getString("tag"),
                        UUID.fromString(rs.getString("owner")), rs.getLong("created"));
                this.clans.put(clan.getId(), clan);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMembers() {
        CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder("SELECT uuid, clan_id FROM clan_members").executeQuerySync();

        if (rs == null) return;

        try {
            while (rs.next()) {
                Clan clan = this.clans.get(UUID.fromString(rs.getString("clan_id")));

                if (clan == null) continue;

                UUID uuid = UUID.fromString(rs.getString("uuid"));
                clan.getMembers().add(uuid);
                this.playerClans.put(uuid, clan);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Clan getClanOfPlayer(UUID uuid) {
        return this.playerClans.get(uuid);
    }

    public Clan getClanByName(String name) {
        for (Clan clan : this.clans.values()) {
            if (clan.getName().equalsIgnoreCase(name)) {
                return clan;
            }
        }
        return null;
    }

    public Clan getClanByTag(String tag) {
        for (Clan clan : this.clans.values()) {
            if (clan.getTag().equalsIgnoreCase(tag)) {
                return clan;
            }
        }
        return null;
    }

    public Clan createClan(String name, String tag, UUID owner) {
        Clan clan = new Clan(UUID.randomUUID(), name, tag, owner, System.currentTimeMillis());
        clan.getMembers().add(owner);
        this.clans.put(clan.getId(), clan);
        this.playerClans.put(owner, clan);

        this.execute("INSERT INTO clans (id, name, tag, owner, created) VALUES (?, ?, ?, ?, ?)",
                clan.getId().toString(), name, tag, owner.toString(), clan.getCreated());
        this.execute("INSERT INTO clan_members (uuid, clan_id) VALUES (?, ?)", owner.toString(), clan.getId().toString());

        this.updateTeam(clan);
        return clan;
    }

    public void deleteClan(Clan clan) {
        for (UUID uuid : clan.getMembers()) {
            this.playerClans.remove(uuid);
        }
        this.clans.remove(clan.getId());
        this.invites.values().forEach(clanIds -> clanIds.remove(clan.getId()));

        this.execute("DELETE FROM clan_members WHERE clan_id = ?", clan.getId().toString());
        this.execute("DELETE FROM clans WHERE id = ?", clan.getId().toString());

        Team team = this.getScoreboard().getTeam(this.getTeamName(clan));

        if (team != null) {
            team.unregister();
        }
    }

    public void addMember(Clan clan, UUID uuid) {
        clan.getMembers().add(uuid);
        this.playerClans.put(uuid, clan);
        this.invites.remove(uuid);

        this.execute("INSERT INTO clan_members (uuid, clan_id) VALUES (?, ?)", uuid.toString(), clan.getId().toString());

        String name = Bukkit.getOfflinePlayer(uuid).getName();

        if (name != null) {
            this.getTeam(clan).addEntry(name);
        }
    }

    public void removeMember(Clan clan, UUID uuid) {
        clan.getMembers().remove(uuid);
        this.playerClans.remove(uuid);

        this.execute("DELETE FROM clan_members WHERE uuid = ?", uuid.toString());

        String name = Bukkit.getOfflinePlayer(uuid).getName();

        if (name != null) {
            this.getTeam(clan).removeEntry(name);
        }
    }

    public boolean invite(Clan clan, UUID uuid) {
        return this.invites.computeIfAbsent(uuid, key -> new HashSet<>()).add(clan.getId());
    }

    public boolean hasInvite(Clan clan, UUID uuid) {
        return this.invites.containsKey(uuid) && this.invites.get(uuid).contains(clan.getId());
    }

    public void sendMessage(Clan clan, String key, String... args) {
        for (UUID uuid : clan.getMembers()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                player.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getMessageForUUID(uuid, key, args));
            }
        }
    }

    public void updatePlayer(Player player) {
        Clan clan = this.getClanOfPlayer(player.getUniqueId());

        if (clan != null) {
            this.getTeam(clan).addEntry(player.getName());
        }
    }

    private void rebuildTeams() {
        for (Team team : new ArrayList<>(this.getScoreboard().getTeams())) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                team.unregister();
            }
        }

        this.clans.values().forEach(this::updateTeam);
    }

    private void updateTeam(Clan clan) {
        Team team = this.getTeam(clan);

        for (UUID uuid : clan.getMembers()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

            if (player.getName() != null) {
                team.addEntry(player.getName());
            }
        }
    }

    private Team getTeam(Clan clan) {
        Team team = this.getScoreboard().getTeam(this.getTeamName(clan));

        if (team == null) {
            team = this.getScoreboard().registerNewTeam(this.getTeamName(clan));
            team.prefix(Component.text("[", NamedTextColor.GRAY)
                    .append(Component.text(clan.getTag(), NamedTextColor.GOLD))
                    .append(Component.text("] ", NamedTextColor.GRAY)));
        }

        return team;
    }

    private String getTeamName(Clan clan) {
        return TEAM_PREFIX + clan.getTag().toLowerCase();
    }

    private Scoreboard getScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private void execute(String sql, Object... params) {
        MinecraftSMP.getInstance().getQueryHandler()
                .createBuilder(sql)
                .addParameters(Arrays.asList(params))
                .executeUpdateSync();
    }
}
