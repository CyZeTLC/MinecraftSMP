package de.cyzetlc.smp.commands;

import com.google.common.base.Joiner;
import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Getter
public abstract class BaseCommand {
    private String command;
    private String permission;
    private String[] aliases;
    private CommandSpecification.TimeUnit cooldownUnit;
    private long cooldownValue;
    private final List<BaseCommand> subCommands = new LinkedList<>();

    protected static Map<String, HashMap<UUID, Long>> cooldowns = new LinkedHashMap<>();

    public void executeCommand(CommandSender sender, String label, String[] args) {
        if (!this.getPermission().equals("") && !sender.hasPermission(this.getPermission())) {
            this.sendMessage(sender, "smp.command.failure.no_permission");
        } else {
            if (sender instanceof Player) {
                Player p = (Player) sender;

                long cooldownEnd = this.getPlayerCooldown(p.getUniqueId());
                long remainingTime = cooldownEnd - System.currentTimeMillis();

                if (cooldownEnd > 0 && remainingTime > 0) {
                    this.sendMessage(sender, "smp.command.failure.cooldown", this.formatTimeToString(remainingTime));
                    return;
                } else {
                    if (this.getCooldownValue() > 0) {
                        HashMap<UUID, Long> time = new HashMap<>();

                        if (!BaseCommand.cooldowns.get(this.getCommand()).isEmpty()) {
                            time = BaseCommand.cooldowns.get(this.getCommand());
                        }
                        time.put(p.getUniqueId(), System.currentTimeMillis() + (this.getCooldownValue() * this.getCooldownUnit().getValue()));

                        BaseCommand.cooldowns.put(this.getCommand(), time);
                    }
                }
            }

            if (!this.getSubCommands().isEmpty() && args.length > 0) {
                if (this.getSubCommand(args[0]) != null) {
                    BaseCommand baseCommand = this.getSubCommand(args[0]);
                    baseCommand.executeCommand(sender,label,args);
                    return;
                }
            }

            this.onCommand(sender,label,args);
        }
    }

    public void sendMessage(CommandSender sender, String key, String... args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            sender.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getMessageForUUID(p.getUniqueId(), key, args));
        } else {
            sender.sendMessage(MinecraftSMP.getInstance().getMessageHandler().getStaticMessage(key, args));
        }
    }

    public String formatTimeToString(long millis) {
        List<String> items = new ArrayList<>();
        long milliseconds = millis % 1000L;
        float seconds = (float)(millis / 1000L % 60L);
        long minutes = millis / 60000L % 60L;
        long hours = millis / 3600000L;
        if (hours >= 24L) {
            return " bis zum " + (new SimpleDateFormat("dd.MM.yyyy um HH.mm")).format(new Date(System.currentTimeMillis() + millis)) + " ";
        } else {
            if (hours > 0L) {
                items.add(hours + " Stunde" + (hours == 1L ? "" : "n"));
            }

            if (minutes > 0L) {
                items.add(minutes + " Minute" + (minutes == 1L ? "" : "n"));
            }

            if (seconds > 0.0F || milliseconds > 0L) {
                items.add((new DecimalFormat("#.##")).format((seconds + this.mapValue((float)milliseconds, 1000.0F, 0.6F))) + " Sekunden");
            }

            return Joiner.on(", ").join(items);
        }
    }

    public BaseCommand getSubCommand(String command) {
        for (BaseCommand baseCommand : this.subCommands) {
            if (baseCommand.command != null) {
                if (baseCommand.command.equals(command.toLowerCase()) || Arrays.asList(baseCommand.aliases).contains(command.toLowerCase())) {
                    return baseCommand;
                }
            }
        }
        return null;
    }

    public void initialize(CommandSpecification spec) {
        this.command = spec.command();
        this.permission = spec.permission();
        this.aliases = spec.aliases();
        this.cooldownUnit = spec.cooldownType();
        this.cooldownValue = spec.cooldownValue();

        cooldowns.put(this.command, new HashMap<>());
    }

    public void registerSubCommand(BaseCommand baseCommand) {
        baseCommand.initialize(baseCommand.getClass().getAnnotation(CommandSpecification.class));
        this.subCommands.add(baseCommand);
    }

    public void register() {
        Command cmd = new Command(this.getCommand()) {
            public boolean execute(CommandSender sender, String label, String[] args) {
                executeCommand(sender, label, args);
                return true;
            }
        };
        cmd.setAliases(Arrays.asList(this.getAliases()));

        CommandMap commandMap = Bukkit.getCommandMap();

        if (commandMap.getCommand(this.getCommand()) != null) {
            commandMap.getCommand(this.getCommand()).unregister(commandMap);
        }

        commandMap.getKnownCommands().remove(this.getCommand());
        commandMap.register(MinecraftSMP.getInstance().getName(), cmd);
    }

    private float mapValue(float value, float maxIn, float maxOut) {
        return maxOut * (value / maxIn);
    }

    public long getPlayerCooldown(UUID uuid) {
        return cooldowns.containsKey(this.command) ? (cooldowns.get(this.command).containsKey(uuid) ? cooldowns.get(this.command).get(uuid) : -1) : -1;
    }

    public abstract void onCommand(CommandSender sender, String label, String[] args);
}