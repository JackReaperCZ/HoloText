package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class UpdateCommand implements Subcommand {
    @Override
    public String name() { return "update"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length < 3) {
            sender.sendMessage("§eUsage: " + usage(sender, label));
            return;
        }
        String name = args[0];
        String flag = args[1];
        if (!flag.equalsIgnoreCase("true") && !flag.equalsIgnoreCase("false")) {
            sender.sendMessage("§cInvalid static flag. Use true or false.");
            return;
        }
        boolean staticRotation = Boolean.parseBoolean(flag);
        String rawText = joinFrom(args, 2);
        var lines = HologramManager.parseTextArg(rawText);
        boolean ok = manager.updateStaticAndText(name, lines, staticRotation);
        sender.sendMessage(ok ? "§aUpdated hologram '" + name + "' (static=" + staticRotation + ")." : "§cNo hologram found with that name.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager) {
        if (args.length == 1) return new java.util.ArrayList<>(manager.names());
        if (args.length == 2) return List.of("true", "false");
        return List.of();
    }

    @Override
    public String usage(CommandSender sender, String label) {
        return (sender instanceof Player ? "/" : "") + label + " update <name> <static:true|false> <text with '|' as newline>";
    }
}