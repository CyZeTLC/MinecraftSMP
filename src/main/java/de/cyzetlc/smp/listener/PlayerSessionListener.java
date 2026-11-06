package de.cyzetlc.smp.listener;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import de.cyzetlc.smp.MinecraftSMP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionListener implements Listener {

    private final MinecraftSMP minecraftSMP;
    private DatabaseReader cityReader;
    private DatabaseReader asnReader;

    private final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();

    public PlayerSessionListener(MinecraftSMP plugin) {
        this.minecraftSMP = plugin;
        try {
            File folder = plugin.getDataFolder();
            this.cityReader = new DatabaseReader.Builder(new File(folder, "GeoLite2-City.mmdb")).build();
            this.asnReader = new DatabaseReader.Builder(new File(folder, "GeoLite2-ASN.mmdb")).build();
        } catch (Exception e) {
            plugin.getLogger().warning("GeoIP-Datenbanken nicht geladen, Geodaten werden übersprungen: " + e.getMessage());
        }
    }

    private void execute(String sql, Object... params) {
        minecraftSMP.getQueryHandler()
                .createBuilder(sql)
                .addParameters(Arrays.asList(params))
                .executeUpdateAsync();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(event.getUniqueId());
        boolean istOp = offlinePlayer.isOp();

        String uuid = String.valueOf(event.getPlayerProfile().getId());
        InetAddress address = event.getAddress();
        String ip = address.getHostAddress();
        String hostname = event.getHostname();

        sessionStart.put(event.getPlayerProfile().getId(), System.currentTimeMillis());

        String countryCode = null, country = null, region = null, city = null, postal = null, timezone = null, isp = null;
        Double lat = null, lon = null;
        Long asn = null;

        if (!address.isAnyLocalAddress() && !address.isLoopbackAddress() && !address.isSiteLocalAddress()) {
            try {
                if (cityReader != null) {
                    CityResponse c = cityReader.city(address);
                    countryCode = c.country().isoCode();
                    country = c.country().name();
                    region = c.mostSpecificSubdivision().name();
                    city = c.city().name();
                    postal = c.postal().code();
                    lat = c.location().latitude();
                    lon = c.location().longitude();
                    timezone = c.location().timeZone();
                }
                if (asnReader != null) {
                    AsnResponse a = asnReader.asn(address);
                    asn = a.autonomousSystemNumber();
                    isp = a.autonomousSystemOrganization();
                }
            } catch (Exception ignored) {
                // IP nicht in der Datenbank
            }
        }

        execute("INSERT INTO player_data (uuid, name, first_login, last_login, login_count, last_ip, last_hostname, "
                        + "country_code, country, region, city, postal_code, latitude, longitude, timezone, asn, isp, is_op) "
                        + "VALUES (?, ?, NOW(), NOW(), 1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE name = VALUES(name), last_login = NOW(), login_count = login_count + 1, "
                        + "last_ip = VALUES(last_ip), last_hostname = VALUES(last_hostname), "
                        + "country_code = VALUES(country_code), country = VALUES(country), region = VALUES(region), "
                        + "city = VALUES(city), postal_code = VALUES(postal_code), latitude = VALUES(latitude), "
                        + "longitude = VALUES(longitude), timezone = VALUES(timezone), asn = VALUES(asn), "
                        + "isp = VALUES(isp), is_op = VALUES(is_op)",
                uuid, event.getPlayerProfile().getName(), ip, hostname,
                countryCode, country, region, city, postal, lat, lon, timezone, asn, isp, istOp);

        execute("INSERT INTO player_ips (uuid, ip, first_seen, last_seen) VALUES (?, ?, NOW(), NOW()) "
                        + "ON DUPLICATE KEY UPDATE last_seen = NOW()",
                uuid, ip);

        execute("INSERT INTO player_sessions (uuid, ip, login_time) VALUES (?, ?, NOW())", uuid, ip);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(minecraftSMP, () -> {
            if (!player.isOnline()) return;

            Location loc = player.getLocation();
            execute("UPDATE player_data SET client_brand = ?, client_locale = ?, ping_ms = ?, "
                            + "last_world = ?, last_x = ?, last_y = ?, last_z = ?, last_gamemode = ? WHERE uuid = ?",
                    player.getClientBrandName(),
                    player.getLocale(),
                    player.getPing(),
                    loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                    player.getGameMode().name(),
                    player.getUniqueId().toString());
        }, 40L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        Long start = sessionStart.remove(player.getUniqueId());
        long durationSeconds = start == null ? 0 : (System.currentTimeMillis() - start) / 1000;

        Location loc = player.getLocation();

        execute("UPDATE player_data SET last_logout = NOW(), playtime_seconds = playtime_seconds + ?, "
                        + "ping_ms = ?, last_world = ?, last_x = ?, last_y = ?, last_z = ?, last_gamemode = ? "
                        + "WHERE uuid = ?",
                durationSeconds, player.getPing(),
                loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(),
                player.getGameMode().name(), uuid);

        execute("UPDATE player_sessions SET logout_time = NOW(), duration_seconds = ? "
                        + "WHERE uuid = ? AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1",
                durationSeconds, uuid);
    }
}