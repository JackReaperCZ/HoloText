package cz.jackreaper.holotext.command;

import cz.jackreaper.holotext.HoloTextPlugin;
import cz.jackreaper.holotext.command.subcommands.*;
import cz.jackreaper.holotext.hologram.HologramManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
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
    private final Map<String, Subcommand> registry = new HashMap<>();

    /**
     * Construct a command handler bound to a hologram manager.
     * @param manager the hologram manager to operate on
     */
    public HoloCommand(HologramManager manager) {
        this.manager = manager;
        register(new CreateCommand());
        register(new DeleteCommand());
        register(new UpdateCommand());
        register(new MoveCommand());
        register(new ListCommand());
        register(new ResetCommand());
        register(new PurgeCommand());
    }

    private void register(Subcommand sub) {
        for (String key : sub.keys()) {
            registry.put(key, sub);
        }
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
            sendUsage(sender, label);
            return true;
        }

        String key = args[0].toLowerCase(Locale.ROOT);
        Subcommand sub = registry.get(key);
        if (sub == null) {
            sendUsage(sender, label);
            return true;
        }
        // pass arguments after the subcommand name
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        sub.execute(sender, label, subArgs, manager);
        return true;
    }

    /**
     * Send the usage overview showing only commands usable by the sender.
     * Player usage uses a leading {@code /}; console usage omits it and shows
     * coordinate-based forms for relevant commands.
     * @param sender command invoker (player or console)
     */
    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§eHoloText commands:");
        // show usage lines from each registered subcommand
        // use LinkedHashSet to avoid duplicates when aliases exist
        Set<Subcommand> unique = new LinkedHashSet<>(registry.values());
        for (Subcommand sub : unique) {
            sender.sendMessage("§7" + sub.usage(sender, label));
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
        if (args.length == 1) {
            // show unique subcommand names (primary names only)
            Set<String> names = registry.values().stream().map(Subcommand::name).collect(Collectors.toCollection(LinkedHashSet::new));
            return StringUtil.copyPartialMatches(args[0], new ArrayList<>(names), new ArrayList<>());
        }
        String key = args[0].toLowerCase(Locale.ROOT);
        Subcommand sub = registry.get(key);
        if (sub == null) return List.of();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        List<String> suggestions = sub.tabComplete(sender, alias, subArgs, manager);
        // copy matches against the current token
        return StringUtil.copyPartialMatches(args[args.length - 1], suggestions, new ArrayList<>());
    }
}