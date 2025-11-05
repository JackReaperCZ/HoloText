package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ResetCommand implements Subcommand {
    @Override
    public String name() { return "reset"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        int loaded = manager.resetFromConfig();
        sender.sendMessage("§aReset complete. Reloaded §f" + loaded + " §ahologram(s) from holograms.yml.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        return (sender instanceof Player ? "/" : "") + label + " reset";
    }
}