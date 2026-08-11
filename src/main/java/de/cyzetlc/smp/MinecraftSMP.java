package de.cyzetlc.smp;

import de.cyzetlc.smp.config.JsonConfig;
import de.cyzetlc.smp.handler.database.MySQLCredentials;
import de.cyzetlc.smp.handler.database.QueryHandler;
import de.cyzetlc.smp.handler.message.MessageHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class MinecraftSMP extends JavaPlugin {
    @Getter
    private static MinecraftSMP instance;

    private QueryHandler queryHandler;
    private MessageHandler messageHandler;
    private ExecutorService executorService;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        this.messageHandler = new MessageHandler();
        this.executorService = Executors.newCachedThreadPool();

        this.loadMySQL();
    }

    private void loadMySQL() {
        this.queryHandler = new QueryHandler(new JsonConfig("./plugins/MinecraftSMP/mysql.json").load(MySQLCredentials.class));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
