package nu.nerd.visit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import net.md_5.bungee.api.ChatColor;

// ----------------------------------------------------------------------------
/**
 * Transient, per-player state, created on join and removed when the player
 * leaves.
 */
public class PlayerState {
    /**
     * Constructor.
     *
     * @param player the player.
     * @param config the configuration from which player preferences are loaded.
     */
    public PlayerState(Player player, YamlConfiguration config) {
        _player = player;
        load(config);
    }

    // ------------------------------------------------------------------------
    /**
     * Save this player's preferences to the specified configuration.
     *
     * @param config the configuration to update.
     */
    public void save(YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection(_player.getUniqueId().toString());
        section.set("name", _player.getName());
        section.set("index", getIndex());
        section.set("period", getPeriod());
        section.set("step", getStep());
        section.set("max", getMax());
        section.set("command", _command != null ? _command : "");
    }

    // ------------------------------------------------------------------------
    /**
     * Load the Player's preferences from the specified configuration
     *
     * @param config the configuration from which player preferences are loaded.
     */
    public void load(YamlConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection(_player.getUniqueId().toString());
        if (section == null) {
            section = config.createSection(_player.getUniqueId().toString());
        }
        setIndex(section.getInt("index", 0));
        setPeriod(section.getInt("period", 10));
        setStep(section.getInt("step", 10));
        setMax(section.getInt("max", 0));
        setCommand(section.getString("command"));
    }

    // ------------------------------------------------------------------------
    /**
     * Start the visitation task.
     */
    public void start() {
        if (_task == null) {
            _task = Bukkit.getScheduler().runTask(NerdVisit.PLUGIN, new Runnable() {
                @Override
                public void run() {
                    visit();
                    if (isVisiting()) {
                        _task = Bukkit.getScheduler().runTaskLater(NerdVisit.PLUGIN,
                                                                   this, 20 * getPeriod());
                    }
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Stop the visitation task.
     */
    public void stop() {
        if (_task != null) {
            _task.cancel();
            _task = null;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the player is being teleported around.
     * 
     * @return true if the player is visiting.
     */
    public boolean isVisiting() {
        return _task != null;
    }

    // ------------------------------------------------------------------------
    /**
     * Perform all of the tasks of a single visitation step.
     * 
     * The current index is validated first, since it may have been invalidated
     * by commands to change the current index, the step size or the maximum
     * coordinate.
     * 
     * The player is messaged if the visitation ends, and if it continues, the
     * player is teleported, a message is sent showing the current progress, and
     * then the command is run, if applicable.
     */
    public void visit() {
        if (getIndex() > getTotalSteps()) {
            setIndex(1);
            stop();
            _player.sendMessage(ChatColor.GOLD + "Visitation complete.");
            return;
        }

        final int side = getSideSteps();
        int i = getIndex() - 1;
        int xIndex = i % side;
        int zIndex = i / side;
        int x = getStep() * xIndex - getMax();
        int z = getStep() * zIndex - getMax();

        Location loc = _player.getLocation();
        loc.setX(x);
        loc.setZ(z);
        _player.teleport(loc);
        _player.sendMessage(ChatColor.GOLD + "Step " +
                            ChatColor.YELLOW + getIndex() +
                            ChatColor.GOLD + " of " +
                            ChatColor.YELLOW + getTotalSteps() +
                            ChatColor.GOLD + ": X: " +
                            ChatColor.YELLOW + (xIndex + 1) + '/' + side +
                            ChatColor.GOLD + ": Z: " +
                            ChatColor.YELLOW + (zIndex + 1) + '/' + side +
                            ChatColor.GOLD + " => " +
                            ChatColor.YELLOW + '(' + x + ", " + z + ')');

        setIndex(getIndex() + 1);
        if (getCommand() != null) {
            // Bukkit doesn't like a leading / in the command.
            String command = getCommand().replaceFirst("/+", "");
            _player.performCommand(command);
        }
    } // visit

    // ------------------------------------------------------------------------
    /**
     * Return the total number of visitation steps along one side of the square
     * (X or Z dimension).
     * 
     * @return the number of steps in the side length.
     */
    public int getSideSteps() {
        return 2 * (_max / _step) + 1;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the total number of visitation steps in the square.
     * 
     * @return the total number of visitation steps.
     */
    public int getTotalSteps() {
        int side = getSideSteps();
        return side * side;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the current 1-based step index.
     * 
     * @param index the index.
     */
    public void setIndex(int index) {
        _index = Math.max(1, index);
    }

    // ------------------------------------------------------------------------
    /**
     * Get the current 0-based step index.
     * 
     * @return the index.
     */
    public int getIndex() {
        return _index;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the period in seconds between teleports.
     * 
     * @param seconds the period in seconds.
     */
    public void setPeriod(int seconds) {
        _period = Math.max(2, seconds);
    }

    // ------------------------------------------------------------------------
    /**
     * Get the period in seconds between teleports.
     * 
     * @return the period in seconds.
     */
    public int getPeriod() {
        return _period;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the distance between teleport locations in blocks.
     * 
     * @param blocks the distance in blocks.
     */
    public void setStep(int blocks) {
        _step = Math.max(5, blocks);
    }

    // ------------------------------------------------------------------------
    /**
     * Get the distance between teleport locations in blocks.
     * 
     * @return the step distance in blocks.
     */
    public int getStep() {
        return _step;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the absolute maximum location coordinate value (i.e. world border
     * size).
     * 
     * @param blocks the maximum magnitude of the coordinate value.
     */
    public void setMax(int blocks) {
        _max = Math.max(0, blocks);
    }

    // ------------------------------------------------------------------------
    /**
     * Get the distance between teleport locations in blocks.
     * 
     * @return the step distance in blocks.
     */
    public int getMax() {
        return _max;
    }

    // ------------------------------------------------------------------------
    /**
     * Set the command to execute at each location.
     * 
     * @param command the command, or null to do nothing.
     */
    public void setCommand(String command) {
        _command = command;
        if (_command != null && _command.length() == 0) {
            _command = null;
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return the command to execute at each location, or null to do nothing.
     * 
     * @return the command.
     */
    public String getCommand() {
        return _command;
    }

    // ------------------------------------------------------------------------
    /**
     * The Player.
     */
    protected Player _player;

    /**
     * Task to visit locations, or null if not currently visiting.
     */
    protected BukkitTask _task;

    /**
     * Current 1-based step index.
     */
    protected int _index;

    /**
     * Period in seconds between teleports.
     */
    protected int _period;

    /**
     * Distance between teleport locations in blocks.
     */
    protected int _step;

    /**
     * Absolute maximum location coordinate value (i.e. world border size).
     */
    protected int _max;

    /**
     * Command to execute after each teleport. Null signifies "no command".
     */
    protected String _command;

} // class PlayerState