package cz.jackreaper.holotext.command;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Command executor and tab completer for the {@code /holo} command.
 *
 * <p>Provides sender-aware usage for subcommands: create, delete, update,
 * move, list, reset, and purge. Tab completion suggests subcommands,
 * hologram names, booleans, worlds, and common radii.
 */
public class HoloCommand implements CommandExecutor, TabCompleter {
    private final HologramManager manager;

    /**
     * Construct a command handler bound to a hologram manager.
     * @param manager the hologram manager to operate on
     */
    public HoloCommand(HologramManager manager) {
        this.manager = manager;
    }

    @Override
    /**
     * Handle {@code /holo} command invocations.
     * <p>Sender must have {@code holotext.admin}. Usage messages are filtered
     * by sender type. Player-only operations (e.g., radius purge around self)
     * are guarded when invoked from console.
     * @return always {@code true} to indicate the command was processed
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("holotext.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create" -> {
                if (args.length < 4) {
                    if (sender instanceof Player) {
                        sender.sendMessage("§eUsage: /" + label + " create <static:true|false> <name> <text with '|' as newline>");
                    } else {
                        sender.sendMessage("§eUsage: " + label + " create <static:true|false> <name> <x> <y> <z> <world> <text with '|' as newline>");
                    }
                    return true;
                }
                String flag = args[1];
                if (!flag.equalsIgnoreCase("true") && !flag.equalsIgnoreCase("false")) {
                    sender.sendMessage("§cInvalid static flag. Use true or false.");
                    return true;
                }
                boolean staticRotation = Boolean.parseBoolean(flag);
                String name = args[2];

                if (!(sender instanceof Player player)) {
                    if (args.length < 8) {
                        sender.sendMessage("§eUsage: " + label + " create <static:true|false> <name> <x> <y> <z> <world> <text with '|' as newline>");
                        return true;
                    }
                    try {
                        double x = Double.parseDouble(args[3]);
                        double y = Double.parseDouble(args[4]);
                        double z = Double.parseDouble(args[5]);
                        String worldName = args[6];
                        var world = HoloTextPlugin.getInstance().getServer().getWorld(worldName);
                        if (world == null) {
                            sender.sendMessage("§cWorld not found: " + worldName);
                            return true;
                        }
                        String rawText = joinFrom(args, 7);
                        var lines = HologramManager.parseTextArg(rawText);
                        if (lines.isEmpty()) {
                            sender.sendMessage("§cText required. Provide at least one character.");
                            return true;
                        }
                        Location loc = new Location(world, x, y, z);
                        boolean ok = manager.create(name, loc, lines, staticRotation);
                        sender.sendMessage(ok ? "§aCreated hologram '" + name + "' (static=" + staticRotation + ") at (" + x + ", " + y + ", " + z + ") in '" + worldName + "'." : "§cA hologram with that name already exists.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid coordinates. Use numbers for x y z.");
                    }
                    return true;
                }

                String rawText = joinFrom(args, 3);
                var lines = HologramManager.parseTextArg(rawText);
                if (lines.isEmpty()) {
                    sender.sendMessage("§cText required. Provide at least one character.");
                    return true;
                }
                Location loc = player.getLocation();
                boolean ok = manager.create(name, loc, lines, staticRotation);
                sender.sendMessage(ok ? "§aCreated hologram '" + name + "' (static=" + staticRotation + ") at your location." : "§cA hologram with that name already exists.");
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: " + (sender instanceof Player ? "/" : "") + label + " delete <name>");
                    return true;
                }
                String name = args[1];
                boolean ok = manager.delete(name);
                sender.sendMessage(ok ? "§aDeleted hologram '" + name + "'." : "§cNo hologram found with that name.");
            }
            case "update" -> {
                if (args.length < 4) {
                    sender.sendMessage("§eUsage: " + (sender instanceof Player ? "/" : "") + label + " update <name> <static:true|false> <text with '|' as newline>");
                    return true;
                }
                String name = args[1];
                String flag = args[2];
                if (!flag.equalsIgnoreCase("true") && !flag.equalsIgnoreCase("false")) {
                    sender.sendMessage("§cInvalid static flag. Use true or false.");
                    return true;
                }
                boolean staticRotation = Boolean.parseBoolean(flag);
                String rawText = joinFrom(args, 3);
                var lines = HologramManager.parseTextArg(rawText);
                boolean ok = manager.updateStaticAndText(name, lines, staticRotation);
                sender.sendMessage(ok ? "§aUpdated hologram '" + name + "' (static=" + staticRotation + ")." : "§cNo hologram found with that name.");
            }
            case "move" -> {
                String name = args.length >= 2 ? args[1] : null;
                if (!(sender instanceof Player player)) {
                    if (args.length < 6) {
                        sender.sendMessage("§eUsage: " + label + " move <name> <x> <y> <z> <world>");
                        return true;
                    }
                    try {
                        double x = Double.parseDouble(args[2]);
                        double y = Double.parseDouble(args[3]);
                        double z = Double.parseDouble(args[4]);
                        String worldName = args[5];
                        var world = HoloTextPlugin.getInstance().getServer().getWorld(worldName);
                        if (world == null) {
                            sender.sendMessage("§cWorld not found: " + worldName);
                            return true;
                        }
                        boolean ok = manager.moveTo(name, new Location(world, x, y, z));
                        sender.sendMessage(ok ? "§aMoved hologram '" + name + "' to (" + x + ", " + y + ", " + z + ") in '" + worldName + "'." : "§cNo hologram found with that name.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid coordinates. Use numbers for x y z.");
                    }
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§eUsage: /" + label + " move <name>");
                    return true;
                }
                boolean ok = manager.moveTo(name, player.getLocation());
                sender.sendMessage(ok ? "§aMoved hologram '" + name + "' to your location." : "§cNo hologram found with that name.");
            }
            case "list" -> {
                var names = manager.names();
                sender.sendMessage("§eHolograms (§f" + names.size() + "§e): §b" + String.join("§7, §b", names));
            }
            case "reset" -> {
                int loaded = manager.resetFromConfig();
                sender.sendMessage("§aReset complete. Reloaded §f" + loaded + " §ahologram(s) from holograms.yml.");
            }
            case "purge" -> {
                if (args.length < 2) {
                    if (sender instanceof Player) {
                        sender.sendMessage("§eUsage: /" + label + " purge <world|radius>");
                    } else {
                        sender.sendMessage("§eUsage: " + label + " purge <world>");
                    }
                    return true;
                }
                if (args.length >= 2) {
                    String area = args[1];
                    try {
                        double radius = Double.parseDouble(area);
                        if (!(sender instanceof Player player)) {
                            sender.sendMessage("§cRadius purge requires a player location. Run in-game or use a world name.");
                            return true;
                        }
                        if (radius <= 0) {
                            sender.sendMessage("§cInvalid radius. Use a positive number of blocks.");
                            return true;
                        }
                        int removed = manager.purgeTaggedWithinRadius(player.getLocation(), radius);
                        sender.sendMessage("§aPurged §f" + removed + " §atagged entity(ies) within §f" + radius + " §ablocks around you.");
                        return true;
                    } catch (NumberFormatException ignored) {
                        var world = HoloTextPlugin.getInstance().getServer().getWorld(area);
                        if (world == null) {
                            sender.sendMessage("§cWorld not found: " + area);
                            return true;
                        }
                        int removed = manager.purgeTaggedInWorld(world);
                        sender.sendMessage("§aPurged §f" + removed + " §atagged entity(ies) in world '§f" + world.getName() + "§a'.");
                        return true;
                    }
                }
            }
            default -> sendUsage(sender);
        }

        return true;
    }

    /**
     * Send the usage overview showing only commands usable by the sender.
     * Player usage uses a leading {@code /}; console usage omits it and shows
     * coordinate-based forms for relevant commands.
     * @param sender command invoker (player or console)
     */
    private void sendUsage(CommandSender sender) {
        boolean isPlayer = sender instanceof Player;
        sender.sendMessage("§eHoloText commands:");
        if (isPlayer) {
            sender.sendMessage("§7/holotext create <static:true|false> <name> <text with '|' as newline>");
        } else {
            sender.sendMessage("§7holo create <static:true|false> <name> <x> <y> <z> <world> <text with '|' as newline>");
        }
        sender.sendMessage("§7" + (isPlayer ? "/holotext" : "holo") + " delete <name>");
        sender.sendMessage("§7" + (isPlayer ? "/holotext" : "holo") + " update <name> <static:true|false> <text with '|' as newline>");
        if (isPlayer) {
            sender.sendMessage("§7/holotext move <name>");
        } else {
            sender.sendMessage("§7holo move <name> <x> <y> <z> <world>");
        }
        sender.sendMessage("§7" + (isPlayer ? "/holotext" : "holo") + " list");
        sender.sendMessage("§7" + (isPlayer ? "/holotext" : "holo") + " reset");
        if (isPlayer) {
            sender.sendMessage("§7/holotext purge <world|radius>");
        } else {
            sender.sendMessage("§7holo purge <world>");
        }
    }

    /**
     * Join arguments from a given index into a single space-separated string.
     * @param args full argument array
     * @param idx starting index (inclusive)
     * @return joined string from {@code idx} onward
     */
    private String joinFrom(String[] args, int idx) {
        return Arrays.stream(args).skip(idx).collect(Collectors.joining(" "));
    }

    @Override
    /**
     * Provide tab completion suggestions based on the current subcommand
     * and argument position. Includes names, booleans, worlds, and common radii.
     */
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions = List.of("create", "delete", "update", "move", "list", "reset", "purge");
            return StringUtil.copyPartialMatches(args[0], suggestions, new ArrayList<>());
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && Arrays.asList("delete", "update", "move").contains(sub)) {
            suggestions = new ArrayList<>(manager.names());
            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }
        // create: suggest boolean at arg2
        if ("create".equals(sub) && args.length == 2) {
            suggestions = List.of("true", "false");
            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }
        // update: suggest boolean at arg3
        if ("update".equals(sub) && args.length == 3) {
            suggestions = List.of("true", "false");
            return StringUtil.copyPartialMatches(args[2], suggestions, new ArrayList<>());
        }
        // move/create console world name at world arg position
        if (("move".equals(sub) && args.length == 6) || ("create".equals(sub) && args.length == 7)) {
            suggestions = HoloTextPlugin.getInstance().getServer().getWorlds().stream().map(w -> w.getName()).collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[args.length - 1], suggestions, new ArrayList<>());
        }
        // purge: suggest worlds or common radii
        if ("purge".equals(sub) && args.length == 2) {
            suggestions = new ArrayList<>(List.of("25", "50", "100", "250"));
            suggestions.addAll(HoloTextPlugin.getInstance().getServer().getWorlds().stream().map(w -> w.getName()).collect(Collectors.toList()));
            return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
        }
        return suggestions;
    }
}