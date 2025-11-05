package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MoveCommand implements Subcommand {
    @Override
    public String name() { return "move"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (sender instanceof Player player) {
            if (args.length < 1) {
                sender.sendMessage("§eUsage: " + usage(sender, label));
                return;
            }
            String name = args[0];
            boolean ok = manager.moveTo(name, player.getLocation());
            sender.sendMessage(ok ? "§aMoved hologram '" + name + "' to your location." : "§cNo hologram found with that name.");
            return;
        }

        if (args.length < 5) {
            sender.sendMessage("§eUsage: " + usage(sender, label));
            return;
        }
        String name = args[0];
        try {
            double x = Double.parseDouble(args[1]);
            double y = Double.parseDouble(args[2]);
            double z = Double.parseDouble(args[3]);
            String worldName = args[4];
            var world = HoloTextPlugin.getInstance().getServer().getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§cWorld not found: " + worldName);
                return;
            }
            boolean ok = manager.moveTo(name, new Location(world, x, y, z));
            sender.sendMessage(ok ? "§aMoved hologram '" + name + "' to (" + x + ", " + y + ", " + z + ") in '" + worldName + "'." : "§cNo hologram found with that name.");
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid coordinates. Use numbers for x y z.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length == 1) return new java.util.ArrayList<>(manager.names());
        if (!(sender instanceof Player) && args.length == 5) {
            return HoloTextPlugin.getInstance().getServer().getWorlds().stream().map(w -> w.getName()).toList();
        }
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        boolean isPlayer = sender instanceof Player;
        return (isPlayer ? "/" : "") + label + (isPlayer ? " move <name>" : " move <name> <x> <y> <z> <world>");
    }
}