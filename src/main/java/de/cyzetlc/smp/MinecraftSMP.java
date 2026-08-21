package de.cyzetlc.smp;

import de.cyzetlc.smp.commands.CommandService;
import de.cyzetlc.smp.commands.ICommandService;
import de.cyzetlc.smp.config.JsonConfig;
import de.cyzetlc.smp.handler.database.MySQLCredentials;
import de.cyzetlc.smp.handler.database.QueryHandler;
import de.cyzetlc.smp.handler.message.MessageHandler;
import de.cyzetlc.smp.listener.PlayerChatListener;
import de.cyzetlc.smp.listener.PlayerConnectionListener;
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
    private ICommandService commandService;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        this.executorService = Executors.newCachedThreadPool();
        this.messageHandler = new MessageHandler();
        this.commandService = new CommandService();

        this.loadMySQL();
        this.loadListeners();
    }

    private void loadListeners() {
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
    }

    private void loadMySQL() {
        this.queryHandler = new QueryHandler(new JsonConfig("./plugins/MinecraftSMP/mysql.json").load(MySQLCredentials.class));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
