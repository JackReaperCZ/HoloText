package cz.jackreaper.holotext.command.subcommands;

import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Defines a subcommand for the /holotext command.
 * Implementations may tailor behavior for player vs console senders.
 */
public interface Subcommand {
    /** @return primary subcommand name (e.g., "create") */
    String name();

    /** @return optional aliases that also map to this subcommand */
    default List<String> aliases() { return List.of(); }

    /**
     * Execute the subcommand.
     * @param sender command invoker
     * @param label root command label (e.g., "holotext")
     * @param args arguments after the subcommand name (i.e., args[0] is first param)
     * @param manager hologram manager for operations
     */
    void execute(CommandSender sender, String label, String[] args, HologramManager manager);

    /**
     * Provide tab completion suggestions for this subcommand.
     * @param sender command invoker
     * @param label root command label
     * @param args arguments after the subcommand name
     * @param manager hologram manager for context
     * @return suggestions for current argument position
     */
    List<String> tabComplete(CommandSender sender, String label, String[] args, HologramManager manager);

    /**
     * @return a usage string appropriate for the sender (player vs console)
     */
    String usage(CommandSender sender, String label);

    /** Convenience to join args from an index. */
    default String joinFrom(String[] args, int idx) {
        return Arrays.stream(args).skip(idx).collect(Collectors.joining(" "));
    }

    /**
     * @return all keys (name + aliases) normalized to lower-case for registration
     */
    default List<String> keys() {
        var list = aliases();
        if (list.isEmpty()) return List.of(name().toLowerCase(Locale.ROOT));
        var all = list.stream().map(a -> a.toLowerCase(Locale.ROOT)).collect(Collectors.toCollection(java.util.ArrayList::new));
        all.add(name().toLowerCase(Locale.ROOT));
        return all;
    }
}