package de.cyzetlc.smp;

import de.cyzetlc.smp.commands.CommandService;
import de.cyzetlc.smp.commands.ICommandService;
import de.cyzetlc.smp.commands.impl.*;
import de.cyzetlc.smp.config.JsonConfig;
import de.cyzetlc.smp.handler.database.MySQLCredentials;
import de.cyzetlc.smp.handler.database.QueryHandler;
import de.cyzetlc.smp.handler.message.MessageHandler;
import de.cyzetlc.smp.listener.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
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

    private JsonConfig jsonConfig;

    private boolean endIsEnabled = false;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        this.executorService = Executors.newCachedThreadPool();
        this.messageHandler = new MessageHandler();
        this.commandService = new CommandService();
        this.jsonConfig = new JsonConfig(this.getName() + "/config.json");

        this.loadMySQL();
        this.loadListeners();
        this.loadCommands();
        this.loadEndEnabledSetting();
    }

    private void loadListeners() {
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DamageListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PortalListener(), this);
    }

    private void loadCommands() {
        this.commandService.registerCommand(new TPACommand());
        this.commandService.registerCommand(new SMPCommand());
        this.commandService.registerCommand(new SpawnCommand());
    }

    private void loadMySQL() {
        this.queryHandler = new QueryHandler(new JsonConfig("./plugins/MinecraftSMP/mysql.json").load(MySQLCredentials.class));
    }

    public void loadEndEnabledSetting() {
        try (CachedRowSet rs = this.queryHandler.createBuilder("SELECT endEnabled FROM worldSettings").executeQuerySync()) {
            endIsEnabled = rs.getBoolean("ebdEnabled");
        } catch (SQLException exception) {
            endIsEnabled = false;
        }
    }

    public void setEndIsEnabled(boolean endIsEnabled) {
        this.endIsEnabled = endIsEnabled;

        this.queryHandler.createBuilder(
                "UPDATE worldSettings SET endEnabled = ?"
        ).addParameter(endIsEnabled).executeUpdateAsync();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
