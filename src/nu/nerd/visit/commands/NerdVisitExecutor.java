package nu.nerd.visit.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nu.nerd.visit.NerdVisit;
import nu.nerd.visit.PlayerState;

// ----------------------------------------------------------------------------
/**
 * CommandExecutor implementation for the /easyrider command.
 */
public class NerdVisitExecutor extends ExecutorBase {
    // ------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public NerdVisitExecutor() {
        super("nerdvisit",
              "help", "status", "start", "stop", "index", "period", "step", "max",
              "command", "clearcomand");
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!inGame(sender)) {
            return true;
        }
        PlayerState state = NerdVisit.PLUGIN.getState((Player) sender);

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            return false;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
            int minutes = (state.getTotalSteps() * state.getPeriod() + 59) / 60;
            sender.sendMessage(ChatColor.GOLD + "Every " +
                               ChatColor.YELLOW + state.getPeriod() +
                               ChatColor.GOLD + " seconds, you will move " +
                               ChatColor.YELLOW + state.getStep() +
                               ChatColor.GOLD + " blocks, ranging between " +
                               ChatColor.YELLOW + "+/-" + state.getMax() +
                               ChatColor.GOLD + ", for approximately " +
                               ChatColor.YELLOW + minutes +
                               ChatColor.GOLD + " minutes in total.");
            if (state.getCommand() != null) {
                sender.sendMessage(ChatColor.GOLD + "At each location, you run: " +
                                   ChatColor.YELLOW + state.getCommand());
            }
            sender.sendMessage(ChatColor.GOLD + "The current step index is " +
                               ChatColor.YELLOW + state.getIndex() +
                               ChatColor.GOLD + " of " +
                               ChatColor.YELLOW + state.getTotalSteps() +
                               ChatColor.GOLD + ".");
            sender.sendMessage(ChatColor.GOLD + "Visitation is " +
                               ChatColor.YELLOW + (state.isVisiting() ? "running" : "stopped") +
                               ChatColor.GOLD + ".");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            state.start();
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
            state.stop();
            return true;
        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("index")) {
            if (args.length == 2) {
                try {
                    state.setIndex(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "The index must be an integer.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GOLD + "The next visited location will be index " +
                               ChatColor.YELLOW + state.getIndex() + ChatColor.GOLD + ".");
            return true;
        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("period")) {
            if (args.length == 2) {
                try {
                    state.setPeriod(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "The period must be an integer.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GOLD + "The period between teleports will be " +
                               ChatColor.YELLOW + state.getPeriod() + ChatColor.GOLD + " seconds.");
            return true;
        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("step")) {
            if (args.length == 2) {
                try {
                    state.setStep(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "The step distance must be an integer.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GOLD + "The step distance between teleports will be " +
                               ChatColor.YELLOW + state.getStep() + ChatColor.GOLD + " blocks.");
            return true;
        } else if ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("max")) {
            if (args.length == 2) {
                try {
                    state.setMax(Integer.parseInt(args[1]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "The maximum visited coordinate must be an integer.");
                    return true;
                }
            }
            sender.sendMessage(ChatColor.GOLD + "The maximum visited coordinate value will be " +
                               ChatColor.YELLOW + "+/-" + state.getMax() + ChatColor.GOLD + ".");
            return true;
        } else if (args.length >= 1 && args[0].equalsIgnoreCase("command")) {
            if (args.length >= 2) {
                String sep = "";
                StringBuilder cmd = new StringBuilder();
                for (int i = 1; i < args.length; ++i) {
                    cmd.append(sep);
                    cmd.append(args[i]);
                    sep = " ";
                }
                state.setCommand(cmd.toString());
            }
            if (state.getCommand() != null) {
                sender.sendMessage(ChatColor.GOLD + "At each location, execute: " + ChatColor.YELLOW + state.getCommand());
            } else {
                sender.sendMessage(ChatColor.GOLD + "No command will be executed at the visited locations.");
            }
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("clearcommand")) {
            state.setCommand(null);
            sender.sendMessage(ChatColor.GOLD + "No command will be executed at the visited locations.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid command. Type \"/" + getName().toLowerCase() + " help\" for help.");
            return true;
        }
    }
} // class NerdVisitExecutor