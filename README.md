NerdVisit
=========
A Bukkit plugin that teleports a player to a sequence of locations in the
current world and optionally runs a command at each place.

`NerdVisit` is intended to be used by admins to perform map-preparation tasks
that require that they run commands in game all over a given world.


Usage
-----
A typical use of `NerdVisit` would be to remove dropped items (e.g. popped
long grass and flowers) from an entire game world when preparing a new map.
First, you should check the world border size:

    /wb list
    
NOTE: `NerdVisit` currently assumes the world is square. It does not traverse
circular worlds.

Say that the world is 6000x6000 with the world border at +/-3000. We configure
`NerdVisit` visitation as follows:

    /nerdvisit index 1
    /nerdvisit max 3000
    /nerdvisit step 100
    /nerdvisit period 3
    /nerdvisit command /remove items 100

This will configure the plugin to move the current player 100 blocks per step 
between -3000 and +3000 along the X and Z axes, teleporting every 3 seconds.
At each step the player will run the command `/remove items 100` in its current
location. Each player can have its own separate `NerdVisit` schedule.

Index 1 is the north west (minus X,  minus Z) corner of the visited area. At 
each step, the current index will increase by 1 until the player reaches the
south east (plus X, plus Z) corner of the area. Visitation will automatically
stop when the configured area has been fully visited. The index will be reset
to 1 in that case, ready for the next complete visit of the area.

You can view information about the visitation schedule with `/nerdvisit status`.
That information includes an estimate of the total time required. You can change
the schedule while visitation is running, but be aware that changing the index,
step size or max area may lead to gaps in the coverage. It is prudent to keep
track of what areas have been visited with a mini-map mod.

To start visiting locations:

    `/nerdvisit start`
    
To stop or pause the visitation:
    
    `/nerdvisit stop`
    
or simply log out. The visitation schedule persists across logins or server
restarts. To continue where you left off, simply `/nerdvisit start`.


Commands
--------

 * `/nerdvisit help` - Show usage help.
 * `/nerdvisit status` - Show the current state of the visitation process.
 * `/nerdvisit start` - Start visiting chunks.
 * `/nerdvisit stop` - Stop or pause visiting chunks.
 * `/nerdvisit index [<value>]` - Show or set the index of the next location to visit.
 * `/nerdvisit period [<value>]` - Show or set the period in seconds between teleports (minimum 2).
 * `/nerdvisit step [<value>]` - Show or set the distance in blocks between visited locations.
 * `/nerdvisit max [<value>]` - Show or set the absolute maximum coordinate to visit.
 * `/nerdvisit command [<command>...]` - Show or set the command to run at each 
   location. Note: in the case of commands that are `CommandHelper` aliases, you
   may have to begin the command with `/runalias`, i.e. 
   `/nerdvisit command /runalias /your-ch-alias-here`. 
 * `/nerdvisit clearcommand` - Clear the command to nothing.


Configuration
-------------
None.


Permissions
-----------

 * `nerdvisit.admin` - Permission to administer and use the plugin.
