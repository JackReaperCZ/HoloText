package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PurgeCommand implements Subcommand {
    @Override
    public String name() { return "purge"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length < 1) {
            sender.sendMessage("§eUsage: " + usage(sender, label));
            return;
        }
        String area = args[0];
        try {
            double radius = Double.parseDouble(area);
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cRadius purge requires a player location. Run in-game or use a world name.");
                return;
            }
            if (radius <= 0) {
                sender.sendMessage("§cInvalid radius. Use a positive number of blocks.");
                return;
            }
            int removed = manager.purgeTaggedWithinRadius(player.getLocation(), radius);
            sender.sendMessage("§aPurged §f" + removed + " §atagged entity(ies) within §f" + radius + " §ablocks around you.");
        } catch (NumberFormatException ignored) {
            var world = HoloTextPlugin.getInstance().getServer().getWorld(area);
            if (world == null) {
                sender.sendMessage("§cWorld not found: " + area);
                return;
            }
            int removed = manager.purgeTaggedInWorld(world);
            sender.sendMessage("§aPurged §f" + removed + " §atagged entity(ies) in world '§f" + world.getName() + "§a'.");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(List.of("25", "50", "100", "250"));
            suggestions.addAll(HoloTextPlugin.getInstance().getServer().getWorlds().stream().map(w -> w.getName()).toList());
            return suggestions;
        }
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        boolean isPlayer = sender instanceof Player;
        return (isPlayer ? "/" : "") + label + " purge " + (isPlayer ? "<world|radius>" : "<world>");
    }
}