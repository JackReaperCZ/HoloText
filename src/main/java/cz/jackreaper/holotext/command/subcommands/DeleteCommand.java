package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeleteCommand implements Subcommand {
    @Override
    public String name() { return "delete"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length < 1) {
            sender.sendMessage("§eUsage: " + usage(sender, label));
            return;
        }
        String name = args[0];
        boolean ok = manager.delete(name);
        sender.sendMessage(ok ? "§aDeleted hologram '" + name + "'." : "§cNo hologram found with that name.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length == 1) return new java.util.ArrayList<>(manager.names());
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        return (sender instanceof Player ? "/" : "") + label + " delete <name>";
    }
}