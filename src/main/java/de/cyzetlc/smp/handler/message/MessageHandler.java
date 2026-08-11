package de.cyzetlc.smp.handler.message;

import com.google.gson.JsonObject;
import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.service.PlayerProfileService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class MessageHandler {
    private final LinkedHashMap<String, String> messages = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> prefixes = new LinkedHashMap<>();

    public MessageHandler() {
        this.prefixes.put("smp", "§7[§c§lSMP§7]");
    }

    /**
     * Add messages from jsonObject
     * @param jsonObject the message to insert
     */
    public void addMessages(JsonObject jsonObject) {
        Iterator<String> keys = jsonObject.keySet().iterator();

        while (keys.hasNext()) {
            String key = keys.next();
            this.messages.put(key, jsonObject.get(key).getAsString());
        }
    }

    /**
     * Loads the messages from database
     */
    public void loadMessages() {
        try {
            CachedRowSet rs = MinecraftSMP.getInstance().getQueryHandler().createBuilder("SELECT `key`,`message` FROM messages").executeQuerySync();

            while (rs.next()) {
                this.messages.put(rs.getString("key"), rs.getString("message"));
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    /**
     * Reloads the messages
     * @param callback the callback which got executed after finishing reload
     */
    public void reload(Consumer<Boolean> callback) {
        MinecraftSMP.getInstance().getExecutorService().execute(() -> {
            MessageHandler.this.messages.clear();
            MessageHandler.this.loadMessages();
            callback.accept(true);
        });
    }

    public void broadcast(String key, String... args) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(this.getMessageForUUID(player.getUniqueId(), key, args)));
    }

    /**
     * Convert message for player
     * @param uuid  the uuid of player
     * @param key   the message key
     * @param args  the parameters which should get inserted
     * @return      the fully converted message for player
     */
    public String getMessageForUUID(UUID uuid, String key, String... args) {
        return this.getStaticMessage((PlayerProfileService.getPlayerProfileSync(uuid).getLanguageKey().equals("de") ? "" :
                PlayerProfileService.getPlayerProfileSync(uuid).getLanguageKey() + ".") + key, args);
    }

    /**
     * Converts the message.
     * Replaces all parameters in message
     * @param key   the message key
     * @param args  the parameters which should get insert into message
     * @return      the fully converted message
     */
    public String getStaticMessage(String key, String... args) {
        String message = "§cNot Found: " + key;
        if (this.messages.containsKey(key)) {
            message = this.getContentOfKey(key);

            for (int i = 0; i < args.length; i++) {
                message = message.replace("{" + i + "}", args[i]);
            }

            if (message.contains("%prefix%")) {
                if (!message.startsWith("en.")) {
                    if (this.prefixes.containsKey(message.split("\\.")[0])) {
                        message = message.replaceFirst("%prefix%", this.prefixes.get(message.split("\\.")[0]));
                    } else {
                        message = message.replaceFirst("%prefix%", this.prefixes.get("smp"));
                    }
                } else {
                    if (this.prefixes.containsKey(message.split("\\.")[1])) {
                        message = message.replaceFirst("%prefix%", this.prefixes.get(message.split("\\.")[1]));
                    } else {
                        message = message.replaceFirst("%prefix%", this.prefixes.get("smp"));
                    }
                }
            }
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * @param key   the message key
     * @return      the content of message
     */
    private String getContentOfKey(String key) {
        return this.messages.get(key);
    }
}
