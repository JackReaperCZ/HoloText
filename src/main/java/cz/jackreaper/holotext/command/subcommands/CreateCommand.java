package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class CreateCommand implements Subcommand {
    @Override
    public String name() { return "create"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        // Player usage: create <static:true|false> <name> <text...>
        // Console usage: create <static:true|false> <name> <x> <y> <z> <world> <text...>
        if (args.length < (sender instanceof Player ? 3 : 7)) {
            sender.sendMessage("§eUsage: " + usage(sender, label));
            return;
        }
        String flag = args[0];
        if (!flag.equalsIgnoreCase("true") && !flag.equalsIgnoreCase("false")) {
            sender.sendMessage("§cInvalid static flag. Use true or false.");
            return;
        }
        boolean staticRotation = Boolean.parseBoolean(flag);
        String name = args[1];

        if (sender instanceof Player player) {
            String rawText = joinFrom(args, 2);
            var lines = HologramManager.parseTextArg(rawText);
            if (lines.isEmpty()) {
                sender.sendMessage("§cText required. Provide at least one character.");
                return;
            }
            Location loc = player.getLocation();
            boolean ok = manager.create(name, loc, lines, staticRotation);
            sender.sendMessage(ok ? "§aCreated hologram '" + name + "' (static=" + staticRotation + ") at your location." : "§cA hologram with that name already exists.");
            return;
        }

        try {
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);
            String worldName = args[5];
            var world = HoloTextPlugin.getInstance().getServer().getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cWorld not found: " + worldName);
                return;
            }
            String rawText = joinFrom(args, 6);
            var lines = HologramManager.parseTextArg(rawText);
            if (lines.isEmpty()) {
                sender.sendMessage("§cText required. Provide at least one character.");
                return;
            }
            Location loc = new Location(world, x, y, z);
            boolean ok = manager.create(name, loc, lines, staticRotation);
            sender.sendMessage(ok ? "§aCreated hologram '" + name + "' (static=" + staticRotation + ") at (" + x + ", " + y + ", " + z + ") in '" + worldName + "'." : "§cA hologram with that name already exists.");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid coordinates. Use numbers for x y z.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        // args: [staticFlag, name, ...] for player; [staticFlag, name, x, y, z, world, ...] for console
        if (args.length == 1) return List.of("true", "false");
        if (!(sender instanceof Player) && args.length == 6) {
            return HoloTextPlugin.getInstance().getServer().getWorlds().stream().map(w -> w.getName()).toList();
        }
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        boolean isPlayer = sender instanceof Player;
        return (isPlayer ? "/" : "") + label + " create <static:true|false> <name> " +
                (isPlayer ? "<text with '|' as newline>" : "<x> <y> <z> <world> <text with '|' as newline>");
    }
}