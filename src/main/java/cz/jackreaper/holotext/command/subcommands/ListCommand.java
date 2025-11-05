package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ListCommand implements Subcommand {
    @Override
    public String name() { return "list"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        var names = manager.names();
        sender.sendMessage("§eHolograms (§f" + names.size() + "§e): §b" + String.join("§7, §b", names));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        return (sender instanceof Player ? "/" : "") + label + " list";
    }
}