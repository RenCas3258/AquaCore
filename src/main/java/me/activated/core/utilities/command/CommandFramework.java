package me.activated.core.utilities.command;

import lombok.Getter;
import me.activated.core.enums.Language;
import me.activated.core.plugin.AquaCore;
import me.activated.core.utilities.chat.Color;
import me.activated.core.utilities.file.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class CommandFramework implements CommandExecutor {

    private Map<String, Entry<Method, Object>> commandMap = new HashMap<String, Entry<Method, Object>>();
    private CommandMap map;
    private JavaPlugin plugin;
    public static CommandFramework instance;

    @Getter private boolean freezeCommand;
    @Getter private Object freezeCommandArgument;

    public static CommandFramework getInstance() {
        return instance;
    }

    public CommandFramework(JavaPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                map = (CommandMap) field.get(manager);
            } catch (IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        this.freezeCommand = true;
        this.freezeCommandArgument = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        return handleCommand(sender, cmd, label, args);
    }

    public boolean handleCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        for (int i = args.length; i >= 0; i--) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(label.toLowerCase());
            for (int x = 0; x < i; x++) {
                buffer.append("." + args[x].toLowerCase());
            }
            String cmdLabel = buffer.toString();
            if (commandMap.containsKey(cmdLabel)) {
                Method method = commandMap.get(cmdLabel).getKey();
                Object methodObject = commandMap.get(cmdLabel).getValue();
                Command command = method.getAnnotation(Command.class);

                if (command.isAdminOnly() && !sender.hasPermission("icefyre.admin")) {
                    sender.sendMessage(Language.NO_PERMISSION.toString()); // TODO change
                    return true;
                }
                if (!command.permission().equals("") && (!sender.hasPermission(command.permission()))) {
                    sender.sendMessage(Language.NO_PERMISSION.toString()); // TODO change
                    return true;
                }
                if (command.inGameOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command is performable by the player only!");
                    return true;
                }

                try {
                    method.invoke(methodObject,
                            new CommandArgs(sender, cmd, label, args, cmdLabel.split("\\.").length - 1));
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        defaultCommand(new CommandArgs(sender, cmd, label, args, 0));
        return true;
    }

    public void loadCommandsInFile() {
        ConfigFile file = new ConfigFile(plugin, "commands.yml");
        file.getKeys(false).forEach(key -> {
            file.set(key, null);
        });
        this.commandMap.forEach((key, value) -> {
            Method method = commandMap.get(key).getKey();
            Object methodObject = commandMap.get(key).getValue();
            Command command = method.getAnnotation(Command.class);

            file.set(command.name() + ".permission", command.permission());
            file.set(command.name() + ".aliases", command.aliases().length > 0 ? command.aliases() : new ArrayList<>());
        });
        file.set("total-commands", file.getKeys(false).size());
        file.save();
    }

    public void registerCommands(Object obj, List<String> aliases) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                boolean parse = false;
                for (String disabled : AquaCore.INSTANCE.getCoreConfig().getStringList("disabled-commands")) {
                    if (command.name().equalsIgnoreCase(disabled)) {
                        parse = true;
                    }
                    if (Arrays.stream(command.aliases()).map(String::toLowerCase).collect(Collectors.toList()).contains(disabled.toLowerCase())) {
                        parse = true;
                    }
                }
                if (!AquaCore.INSTANCE.getCoreConfig().getBoolean("coins.enabled")) {
                    if (command.name().equalsIgnoreCase("coins")) {
                        parse = true;
                    }
                }
                if (parse && command.name().equalsIgnoreCase("freeze")) {
                    this.freezeCommand = false;
                }
                if (parse) {
                    Bukkit.getConsoleSender().sendMessage(Color.translate("&bCommand &3'" + command.name() + "' &bhas been &cdisabled &bdue to configuration!"));
                    continue;
                }
                if (command.name().equalsIgnoreCase("gfreeze")) {
                    this.freezeCommandArgument = obj;
                }
                registerCommand(command, command.name(), m, obj);

                for (String alias : command.aliases()) {
                    registerCommand(command, alias, m, obj);
                }
                if (aliases != null) {
                    for (String alias : aliases) {
                        registerCommand(command, alias, m, obj);
                    }
                }
            } else if (m.getAnnotation(Completer.class) != null) {
                Completer comp = m.getAnnotation(Completer.class);
                if (m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0
                        || m.getParameterTypes()[0] != CommandArgs.class) {
                    System.out.println(
                            "Unable to register tab completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }
                if (m.getReturnType() != List.class) {
                    System.out.println("Unable to register tab completer " + m.getName() + ". Unexpected return type");
                    continue;
                }
                registerCompleter(comp.name(), m, obj);
                for (String alias : comp.aliases()) {
                    registerCompleter(alias, m, obj);
                }
            }
        }
    }

    public void registerHelp() {
        Set<HelpTopic> help = new TreeSet<HelpTopic>(HelpTopicComparator.helpTopicComparatorInstance());
        for (String s : commandMap.keySet()) {
            if (!s.contains(".")) {
                org.bukkit.command.Command cmd = map.getCommand(s);
                HelpTopic topic = new GenericCommandHelpTopic(cmd);
                help.add(topic);
            }
        }
        IndexHelpTopic topic = new IndexHelpTopic(plugin.getName(), "All commands for " + plugin.getName(), null, help,
                "Below is a list of all " + plugin.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    public void unregisterCommands(Object obj) {
        for (Method m : obj.getClass().getMethods()) {
            if (m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                commandMap.remove(command.name().toLowerCase());
                commandMap.remove(this.plugin.getName() + ":" + command.name().toLowerCase());
                map.getCommand(command.name().toLowerCase()).unregister(map);
            }
        }
    }

    public void registerCommand(Command command, String label, Method m, Object obj) {
        commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        commandMap.put(this.plugin.getName() + ':' + label.toLowerCase(),
                new AbstractMap.SimpleEntry<Method, Object>(m, obj));
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, this, plugin);
            map.register(plugin.getName(), cmd);
        }
        if (!command.description().equalsIgnoreCase("") && cmdLabel == label) {
            map.getCommand(cmdLabel).setDescription(command.description());
        }
        if (!command.usage().equalsIgnoreCase("") && cmdLabel == label) {
            map.getCommand(cmdLabel).setUsage(command.usage());
        }
    }

    public void registerCompleter(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();
        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(cmdLabel, this, plugin);
            map.register(plugin.getName(), command);
        }
        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);
            if (command.completer == null) {
                command.completer = new BukkitCompleter();
            }
            command.completer.addCompleter(label, m, obj);
        } else if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = map.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);
                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tab completer " + m.getName()
                            + ". A tab completer is already registered for that command!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void defaultCommand(CommandArgs args) {
        args.getSender().sendMessage(args.getLabel() + " is not handled! Oh noes!");
    }
}
