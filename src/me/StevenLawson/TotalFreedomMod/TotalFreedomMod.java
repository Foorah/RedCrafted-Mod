package me.StevenLawson.TotalFreedomMod;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class TotalFreedomMod extends JavaPlugin
{
    public TotalFreedomMod tfm = this;
    
    private final TFM_EntityListener entityListener = new TFM_EntityListener(this);
    private final TFM_BlockListener blockListener = new TFM_BlockListener(this);
    private final TFM_PlayerListener playerListener = new TFM_PlayerListener(this);
    
    private static final Logger log = Logger.getLogger("Minecraft");
    
    protected static Configuration CONFIG;
    public List<String> superadmins = new ArrayList<String>();
    public List<String> superadmin_ips = new ArrayList<String>();
    public Boolean allowExplosions = false;
    public boolean allowFirePlace = false;
    public Boolean allowFireSpread = false;
    public Boolean allowLavaDamage = false;
    public boolean allowLavaPlace = false;
    public boolean allowWaterPlace = false;
    public boolean autoEntityWipe = false;
    public double explosiveRadius = 4.0;
    public boolean nukeMonitor = true;
    public int nukeMonitorCount = 40;
    public double nukeMonitorRange = 10.0;
    public Boolean preprocessLogEnabled = false;
    
    public boolean allPlayersFrozen = false;
    public HashMap userinfo = new HashMap();
    
    public final static String MSG_NO_PERMS = ChatColor.YELLOW + "You do not have permission to use this command.";
    public final static String YOU_ARE_OP = ChatColor.YELLOW + "You are now op!";
    public final static String YOU_ARE_NOT_OP = ChatColor.YELLOW + "You are no longer op!";
    public final static String CAKE_LYRICS = "But there's no sense crying over every mistake. You just keep on trying till you run out of cake.";

    @Override
    public void onEnable()
    {
        CONFIG = getConfiguration();
        File configfile = new File("plugins/TotalFreedomMod/config.yml");
        if (!configfile.exists())
        {
            log.log(Level.INFO, "[Total Freedom Mod] - Generating default config file (plugins/TotalFreedomMod/config.yml)...");
            CONFIG.setProperty("superadmins", new String[]
                    {
                        "Madgeek1450", "markbyron"
                    });
            CONFIG.setProperty("superadmin_ips", new String[]
                    {
                        "0.0.0.0"
                    });
            CONFIG.setProperty("allow_explosions", false);
            CONFIG.setProperty("allow_fire_place", false);
            CONFIG.setProperty("allow_fire_spread", false);
            CONFIG.setProperty("allow_lava_damage", false);
            CONFIG.setProperty("allow_lava_place", false);
            CONFIG.setProperty("allow_water_place", false);
            CONFIG.setProperty("auto_wipe", false);
            CONFIG.setProperty("explosiveRadius", 4.0);
            CONFIG.setProperty("nuke_monitor", true);
            CONFIG.setProperty("nuke_monitor_count", 40);
            CONFIG.setProperty("nuke_monitor_range", 10.0);
            CONFIG.setProperty("preprocess_log", false);
            CONFIG.save();
        }
        CONFIG.load();
        superadmins = CONFIG.getStringList("superadmins", null);
        superadmin_ips = CONFIG.getStringList("superadmin_ips", null);
        allowExplosions = CONFIG.getBoolean("allow_explosions", false);
        allowFirePlace = CONFIG.getBoolean("allow_fire_place", false);
        allowFireSpread = CONFIG.getBoolean("allow_fire_spread", false);
        allowLavaDamage = CONFIG.getBoolean("allow_lava_damage", false);
        allowLavaPlace = CONFIG.getBoolean("allow_lava_place", false);
        allowWaterPlace = CONFIG.getBoolean("allow_water_place", false);
        autoEntityWipe = CONFIG.getBoolean("auto_wipe", false);
        explosiveRadius = CONFIG.getDouble("explosiveRadius", 4.0);
        nukeMonitor = CONFIG.getBoolean("nuke_monitor", true);
        nukeMonitorCount = CONFIG.getInt("nuke_monitor_count", 40);
        nukeMonitorRange = CONFIG.getDouble("nuke_monitor_range", 10.0);
        preprocessLogEnabled = CONFIG.getBoolean("preprocess_log", false);

        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvent(Event.Type.ENTITY_EXPLODE, entityListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.ENTITY_COMBUST, entityListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.EXPLOSION_PRIME, entityListener, Event.Priority.High, this);

        pm.registerEvent(Event.Type.BLOCK_IGNITE, blockListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_BURN, blockListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Event.Priority.Normal, this);

        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.High, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Normal, this);

        log.log(Level.INFO, "[Total Freedom Mod] - Enabled! - Version: " + this.getDescription().getVersion() + " by Madgeek1450");
        log.log(Level.INFO, "[Total Freedom Mod] - Loaded superadmin names: " + implodeStringList(", ", superadmins));
        log.log(Level.INFO, "[Total Freedom Mod] - Loaded superadmin IPs: " + implodeStringList(", ", superadmins));
        log.log(Level.INFO, "[Total Freedom Mod] - Auto drop deleter is " + (autoEntityWipe ? "enabled" : "disabled") + ".");

        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                tfm.tfHeartbeat();
            }
        }, 100L, 100L);
    }

    @Override
    public void onDisable()
    {
        log.log(Level.INFO, "[Total Freedom Mod] - Disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        try
        {
            Player player = null;
            if (sender instanceof Player)
            {
                player = (Player) sender;
                log.log(Level.INFO, String.format("[PLAYER_COMMAND] %s(%s): /%s %s", player.getName(), ChatColor.stripColor(player.getDisplayName()), commandLabel, implodeStringList(" ", Arrays.asList(args))));
            }
            else
            {
                log.log(Level.INFO, String.format("[CONSOLE_COMMAND] %s: /%s %s", sender.getName(), commandLabel, implodeStringList(" ", Arrays.asList(args))));
            }

            if (cmd.getName().equalsIgnoreCase("opme"))
            {
                if (player == null)
                {
                    sender.sendMessage("This command only works in-game.");
                }
                else
                {
                    if (isUserSuperadmin(sender))
                    {
                        tfBroadcastMessage(String.format("(%s: Opping %s)", sender.getName(), sender.getName()), ChatColor.GRAY);
                        sender.setOp(true);
                        sender.sendMessage(YOU_ARE_OP);
                    }
                    else
                    {
                        sender.sendMessage(MSG_NO_PERMS);
                    }
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("listreal") || cmd.getName().equalsIgnoreCase("list"))
            {
                StringBuilder onlineStats = new StringBuilder();
                StringBuilder onlineUsers = new StringBuilder();

                if (player == null)
                {
                    onlineStats.append(String.format("There are %d out of a maximum %d players online.", Bukkit.getOnlinePlayers().length, Bukkit.getMaxPlayers()));

                    onlineUsers.append("Connected players: ");
                    boolean first = true;
                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            onlineUsers.append(", ");
                        }

                        if (sender.getName().equalsIgnoreCase("remotebukkit"))
                        {
                            onlineUsers.append(p.getName());
                        }
                        else
                        {
                            if (p.isOp())
                            {
                                onlineUsers.append("[OP]").append(p.getName());
                            }
                            else
                            {
                                onlineUsers.append(p.getName());
                            }
                        }
                    }
                }
                else
                {
                    onlineStats.append(ChatColor.BLUE).append("There are ").append(ChatColor.RED).append(Bukkit.getOnlinePlayers().length);
                    onlineStats.append(ChatColor.BLUE).append(" out of a maximum ").append(ChatColor.RED).append(Bukkit.getMaxPlayers());
                    onlineStats.append(ChatColor.BLUE).append(" players online.");

                    onlineUsers.append("Connected players: ");
                    boolean first = true;
                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        if (first)
                        {
                            first = false;
                        }
                        else
                        {
                            onlineUsers.append(", ");
                        }

                        if (p.isOp())
                        {
                            onlineUsers.append(ChatColor.RED).append(p.getName());
                        }
                        else
                        {
                            onlineUsers.append(p.getName());
                        }

                        onlineUsers.append(ChatColor.WHITE);
                    }
                }

                sender.sendMessage(onlineStats.toString());
                sender.sendMessage(onlineUsers.toString());

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("deopall"))
            {
                if (isUserSuperadmin(sender) || player == null)
                {
                    tfBroadcastMessage(String.format("(%s: De-opping everyone)", sender.getName()), ChatColor.GRAY);

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        if (!isUserSuperadmin(p) && !p.getName().equals(sender.getName()))
                        {
                            p.setOp(false);
                            p.sendMessage(YOU_ARE_NOT_OP);
                        }
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("opall"))
            {
                if (isUserSuperadmin(sender) || player == null)
                {
                    tfBroadcastMessage(String.format("(%s: Opping everyone)", sender.getName()), ChatColor.GRAY);

                    boolean doSetGamemode = false;
                    GameMode targetGamemode = GameMode.CREATIVE;
                    if (args.length != 0)
                    {
                        if (args[0].equals("-c"))
                        {
                            doSetGamemode = true;
                            targetGamemode = GameMode.CREATIVE;
                        }
                        else if (args[0].equals("-s"))
                        {
                            doSetGamemode = true;
                            targetGamemode = GameMode.SURVIVAL;
                        }
                    }

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        p.setOp(true);
                        p.sendMessage(YOU_ARE_OP);

                        if (doSetGamemode)
                        {
                            p.setGameMode(targetGamemode);
                        }
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("qop")) //Quick OP
            {
                if (args.length != 1)
                {
                    return false;
                }

                if (sender.isOp() || player == null || isUserSuperadmin(sender))
                {
                    boolean matched_player = false;
                    for (Player p : Bukkit.matchPlayer(args[0]))
                    {
                        matched_player = true;

                        tfBroadcastMessage(String.format("(%s: Opping %s)", sender.getName(), p.getName()), ChatColor.GRAY);
                        p.setOp(true);
                        p.sendMessage(YOU_ARE_OP);
                    }
                    if (!matched_player)
                    {
                        sender.sendMessage("No targets matched.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("qdeop")) //Quick De-op
            {
                if (args.length != 1)
                {
                    return false;
                }

                if (sender.isOp() || player == null || isUserSuperadmin(sender))
                {
                    boolean matched_player = false;
                    for (Player p : Bukkit.matchPlayer(args[0]))
                    {
                        matched_player = true;

                        tfBroadcastMessage(String.format("(%s: De-opping %s)", sender.getName(), p.getName()), ChatColor.GRAY);
                        p.setOp(false);
                        p.sendMessage(YOU_ARE_NOT_OP);
                    }
                    if (!matched_player)
                    {
                        sender.sendMessage("No targets matched.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("survival"))
            {
                if (player == null)
                {
                    if (args.length == 0)
                    {
                        sender.sendMessage("When used from the console, you must define a target user to change gamemode on.");
                        return true;
                    }
                }
                else
                {
                    if (!sender.isOp())
                    {
                        sender.sendMessage(MSG_NO_PERMS);
                        return true;
                    }
                }

                Player p;
                if (args.length == 0)
                {
                    p = Bukkit.getPlayerExact(sender.getName());
                }
                else
                {
                    List<Player> matches = Bukkit.matchPlayer(args[0]);
                    if (matches.isEmpty())
                    {
                        sender.sendMessage("Can't find user " + args[0]);
                        return true;
                    }
                    else
                    {
                        p = matches.get(0);
                    }
                }

                sender.sendMessage("Setting " + p.getName() + " to game mode 'Survival'.");
                p.sendMessage(sender.getName() + " set your game mode to 'Survival'.");
                p.setGameMode(GameMode.SURVIVAL);

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("creative"))
            {
                if (player == null)
                {
                    if (args.length == 0)
                    {
                        sender.sendMessage("When used from the console, you must define a target user to change gamemode on.");
                        return true;
                    }
                }
                else
                {
                    if (!sender.isOp())
                    {
                        sender.sendMessage(MSG_NO_PERMS);
                        return true;
                    }
                }

                Player p;
                if (args.length == 0)
                {
                    p = Bukkit.getPlayerExact(sender.getName());
                }
                else
                {
                    List<Player> matches = Bukkit.matchPlayer(args[0]);
                    if (matches.isEmpty())
                    {
                        sender.sendMessage("Can't find user " + args[0]);
                        return true;
                    }
                    else
                    {
                        p = matches.get(0);
                    }
                }

                sender.sendMessage("Setting " + p.getName() + " to game mode 'Creative'.");
                p.sendMessage(sender.getName() + " set your game mode to 'Creative'.");
                p.setGameMode(GameMode.CREATIVE);

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("wildcard"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args[0].equals("wildcard"))
                    {
                        sender.sendMessage("What the hell are you trying to do, you stupid idiot...");
                        return true;
                    }

                    String base_command = implodeStringList(" ", Arrays.asList(args));

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        String out_command = base_command.replaceAll("\\x3f", p.getName());
                        sender.sendMessage("Running Command: " + out_command);
                        Bukkit.getServer().dispatchCommand(sender, out_command);
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("say"))
            {
                if (args.length == 0)
                {
                    return false;
                }

                if (player == null || sender.isOp())
                {
                    String message = implodeStringList(" ", Arrays.asList(args));
                    tfBroadcastMessage(String.format("[Server:%s] %s", sender.getName(), message), ChatColor.LIGHT_PURPLE);
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("gtfo"))
            {
                if (args.length != 1)
                {
                    return false;
                }

                if (player == null || isUserSuperadmin(sender))
                {
                    Player p;
                    List<Player> matches = Bukkit.matchPlayer(args[0]);
                    if (matches.isEmpty())
                    {
                        sender.sendMessage("Can't find user " + args[0]);
                        return true;
                    }
                    else
                    {
                        p = matches.get(0);
                    }

                    tfBroadcastMessage(p.getName() + " has been a naughty, naughty boy.", ChatColor.RED);

                    //Deop
                    p.setOp(false);

                    //Set gamemode to survival:
                    p.setGameMode(GameMode.SURVIVAL);

                    //Clear inventory:
                    p.getInventory().clear();

                    //Strike with lightning effect:
                    final Location target_pos = p.getLocation();
                    for (int x = -1; x <= 1; x++)
                    {
                        for (int z = -1; z <= 1; z++)
                        {
                            final Location strike_pos = new Location(target_pos.getWorld(), target_pos.getBlockX() + x, target_pos.getBlockY(), target_pos.getBlockZ() + z);
                            target_pos.getWorld().strikeLightning(strike_pos);
                        }
                    }

                    //Attempt to kill:
                    p.setHealth(0);

                    //Ban IP Address:
                    String user_ip = p.getAddress().getAddress().toString().replaceAll("/", "").trim();
                    tfBroadcastMessage(String.format("Banning: %s, IP: %s.", p.getName(), user_ip), ChatColor.RED);
                    Bukkit.banIP(user_ip);

                    //Ban Username:
                    Bukkit.getOfflinePlayer(p.getName()).setBanned(true);

                    //Kick Player:
                    p.kickPlayer("GTFO");
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("stop"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    tfBroadcastMessage("Server is going offline.", ChatColor.GRAY);

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        p.kickPlayer("Server is going offline, come back in a few minutes.");
                    }

                    Bukkit.shutdown();
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("explosives"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length == 0)
                    {
                        return false;
                    }

                    if (args.length == 2)
                    {
                        explosiveRadius = Double.parseDouble(args[1]);
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowExplosions = true;
                        sender.sendMessage("Explosives are now enabled, radius set to " + explosiveRadius + " blocks.");
                    }
                    else
                    {
                        this.allowExplosions = false;
                        sender.sendMessage("Explosives are now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("firespread"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowFireSpread = true;
                        sender.sendMessage("Fire spread is now enabled.");
                    }
                    else
                    {
                        this.allowFireSpread = false;
                        sender.sendMessage("Fire spread is now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("fireplace"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowFirePlace = true;
                        sender.sendMessage("Fire placement is now enabled.");
                    }
                    else
                    {
                        this.allowFirePlace = false;
                        sender.sendMessage("Fire placement is now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("lavadmg"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowLavaDamage = true;
                        sender.sendMessage("Lava damage is now enabled.");
                    }
                    else
                    {
                        this.allowLavaDamage = false;
                        sender.sendMessage("Lava damage is now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("lavaplace"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowLavaPlace = true;
                        sender.sendMessage("Lava placement is now enabled.");
                    }
                    else
                    {
                        this.allowLavaPlace = false;
                        sender.sendMessage("Lava placement is now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("waterplace"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.allowWaterPlace = true;
                        sender.sendMessage("Water placement is now enabled.");
                    }
                    else
                    {
                        this.allowWaterPlace = false;
                        sender.sendMessage("Water placement is now disabled.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("radar"))
            {
                if (player == null)
                {
                    sender.sendMessage("This command can only be used in-game.");
                    return true;
                }

                Player sender_player = Bukkit.getPlayerExact(sender.getName());
                Location sender_pos = sender_player.getLocation();
                String sender_world = sender_player.getWorld().getName();

                List<TFM_RadarData> radar_data = new ArrayList<TFM_RadarData>();

                for (Player p : Bukkit.getOnlinePlayers())
                {
                    if (sender_world.equals(p.getWorld().getName()) && !p.getName().equals(sender.getName()))
                    {
                        radar_data.add(new TFM_RadarData(p, sender_pos.distance(p.getLocation()), p.getLocation()));
                    }
                }

                Collections.sort(radar_data, new TFM_RadarData());

                sender.sendMessage(ChatColor.YELLOW + "People nearby in " + sender_world + ":");

                int countmax = 5;
                if (args.length == 1)
                {
                    countmax = Integer.parseInt(args[0]);
                }

                int count = 0;
                for (TFM_RadarData i : radar_data)
                {
                    if (count++ > countmax)
                    {
                        break;
                    }

                    sender.sendMessage(ChatColor.YELLOW + String.format("%s - %d blocks away @ %s.", i.player.getName(), Math.round(i.distance), formatLocation(i.location)));
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("rd"))
            {
                if (player == null || sender.isOp())
                {
                    sender.sendMessage(ChatColor.GRAY + "Removing all dropped items, arrows, exp. orbs and TNT...");
                    sender.sendMessage(ChatColor.GRAY + String.valueOf(wipeDropEntities()) + " dropped enties removed.");
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("mp"))
            {
                if (player == null || sender.isOp())
                {
                    sender.sendMessage(ChatColor.GRAY + "Purging all mobs...");

                    int removed = 0;
                    for (World world : Bukkit.getWorlds())
                    {
                        for (Entity ent : world.getEntities())
                        {
                            if (ent instanceof Creature || ent instanceof Ghast || ent instanceof Slime)
                            {
                                ent.remove();
                                removed++;
                            }
                        }
                    }

                    sender.sendMessage(ChatColor.GRAY + String.valueOf(removed) + " mobs removed.");
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("prelog"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length != 1)
                    {
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.preprocessLogEnabled = true;
                        sender.sendMessage("Command preprocess logging is now enabled. This will be spammy in the log.");
                    }
                    else
                    {
                        this.preprocessLogEnabled = false;
                        sender.sendMessage("Command preprocess logging is now disabled.");
                    }

                    CONFIG.load();
                    CONFIG.setProperty("preprocess_log", this.preprocessLogEnabled);
                    CONFIG.save();
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("gadmin"))
            {
                if (args.length == 0)
                {
                    return false;
                }

                String mode = args[0].toLowerCase();

                if (player == null || isUserSuperadmin(sender))
                {
                    if (mode.equals("list"))
                    {
                        sender.sendMessage(ChatColor.GRAY + "[ Real Name ] : [ Display Name ] - Hash:");
                    }

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        String hash = p.getUniqueId().toString().substring(0, 4);
                        if (mode.equals("list"))
                        {
                            sender.sendMessage(ChatColor.GRAY + String.format("[ %s ] : [ %s ] - %s",
                                    p.getName(),
                                    ChatColor.stripColor(p.getDisplayName()),
                                    hash));
                        }
                        else if (hash.equalsIgnoreCase(args[1]))
                        {
                            if (mode.equals("kick"))
                            {
                                p.kickPlayer("Kicked by Administrator");
                            }
                            else if (mode.equals("nameban"))
                            {
                                Bukkit.getOfflinePlayer(p.getName()).setBanned(true);
                                tfBroadcastMessage(String.format("Banning Name: %s.", p.getName()), ChatColor.RED);
                                p.kickPlayer("Username banned by Administrator.");
                            }
                            else if (mode.equals("ipban"))
                            {
                                String user_ip = p.getAddress().getAddress().toString().replaceAll("/", "").trim();
                                tfBroadcastMessage(String.format("Banning IP: %s.", p.getName(), user_ip), ChatColor.RED);
                                Bukkit.banIP(user_ip);
                                p.kickPlayer("IP address banned by Administrator.");
                            }
                            else if (mode.equals("ban"))
                            {
                                String user_ip = p.getAddress().getAddress().toString().replaceAll("/", "").trim();
                                tfBroadcastMessage(String.format("Banning Name: %s, IP: %s.", p.getName(), user_ip), ChatColor.RED);
                                Bukkit.banIP(user_ip);
                                Bukkit.getOfflinePlayer(p.getName()).setBanned(true);
                                p.kickPlayer("IP and username banned by Administrator.");
                            }
                            else if (mode.equals("op"))
                            {
                                tfBroadcastMessage(String.format("(%s: Opping %s)", sender.getName(), p.getName()), ChatColor.GRAY);
                                p.setOp(false);
                                p.sendMessage(YOU_ARE_OP);
                            }
                            else if (mode.equals("deop"))
                            {
                                tfBroadcastMessage(String.format("(%s: De-opping %s)", sender.getName(), p.getName()), ChatColor.GRAY);
                                p.setOp(false);
                                p.sendMessage(YOU_ARE_NOT_OP);
                            }
                            else if (mode.equals("ci"))
                            {
                                p.getInventory().clear();
                            }
                            else if (mode.equals("fr"))
                            {
                                TFM_UserInfo playerdata = (TFM_UserInfo) this.userinfo.get(p);
                                if (playerdata != null)
                                {
                                    playerdata.setFrozen(!playerdata.isFrozen());
                                }
                                else
                                {
                                    playerdata = new TFM_UserInfo();
                                    playerdata.setFrozen(true);
                                    this.userinfo.put(p, playerdata);
                                }
                                sender.sendMessage(ChatColor.AQUA + p.getName() + " has been " + (playerdata.isFrozen() ? "frozen" : "unfrozen") + ".");
                                p.sendMessage(ChatColor.AQUA + "You have been " + (playerdata.isFrozen() ? "frozen" : "unfrozen") + ".");
                            }

                            return true;
                        }
                    }

                    if (!mode.equals("list"))
                    {
                        sender.sendMessage(ChatColor.RED + "Invalid hash.");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("status"))
            {
                sender.sendMessage(ChatColor.GRAY + "Server is currently running with 'online-mode=" + (Bukkit.getOnlineMode() ? "true" : "false") + "'.");

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("fr"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length == 0)
                    {
                        this.allPlayersFrozen = !this.allPlayersFrozen;

                        if (this.allPlayersFrozen)
                        {
                            this.allPlayersFrozen = true;
                            sender.sendMessage("Players are now frozen.");
                            tfBroadcastMessage(sender.getName() + " has temporarily frozen everyone on the server.", ChatColor.AQUA);
                        }
                        else
                        {
                            this.allPlayersFrozen = false;
                            sender.sendMessage("Players are now free to move.");
                            tfBroadcastMessage(sender.getName() + " has unfrozen everyone.", ChatColor.AQUA);
                        }
                    }
                    else
                    {
                        Player p;
                        List<Player> matches = Bukkit.matchPlayer(args[0]);
                        if (matches.isEmpty())
                        {
                            sender.sendMessage("Can't find user " + args[0]);
                            return true;
                        }
                        else
                        {
                            p = matches.get(0);
                        }

                        TFM_UserInfo playerdata = (TFM_UserInfo) this.userinfo.get(p);
                        if (playerdata != null)
                        {
                            playerdata.setFrozen(!playerdata.isFrozen());
                        }
                        else
                        {
                            playerdata = new TFM_UserInfo();
                            playerdata.setFrozen(true);
                            this.userinfo.put(p, playerdata);
                        }

                        sender.sendMessage(ChatColor.AQUA + p.getName() + " has been " + (playerdata.isFrozen() ? "frozen" : "unfrozen") + ".");
                        p.sendMessage(ChatColor.AQUA + "You have been " + (playerdata.isFrozen() ? "frozen" : "unfrozen") + ".");
                    }
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("nonuke"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    if (args.length < 1)
                    {
                        return false;
                    }

                    if (args.length >= 2)
                    {
                        this.nukeMonitorRange = Double.parseDouble(args[1]);
                    }

                    if (args.length >= 3)
                    {
                        this.nukeMonitorCount = Integer.parseInt(args[2]);
                    }

                    if (args[0].equalsIgnoreCase("on"))
                    {
                        this.nukeMonitor = true;
                        sender.sendMessage(ChatColor.GRAY + "Nuke monitor is enabled.");
                        sender.sendMessage(ChatColor.GRAY + "Anti-freecam range is set to " + this.nukeMonitorRange + " blocks.");
                        sender.sendMessage(ChatColor.GRAY + "Block throttle rate is set to " + this.nukeMonitorCount + " blocks destroyed per 5 seconds.");
                    }
                    else
                    {
                        this.nukeMonitor = false;
                        sender.sendMessage("Nuke monitor is disabled.");
                    }

                    CONFIG.load();
                    CONFIG.setProperty("nuke_monitor", this.nukeMonitor);
                    CONFIG.setProperty("nuke_monitor_range", this.nukeMonitorRange);
                    CONFIG.setProperty("nuke_monitor_count", this.nukeMonitorCount);
                    CONFIG.save();
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
            else if (cmd.getName().equalsIgnoreCase("cake"))
            {
                if (player == null || isUserSuperadmin(sender))
                {
                    StringBuilder output = new StringBuilder();
                    Random randomGenerator = new Random();

                    for (String word : CAKE_LYRICS.split(" "))
                    {
                        String color_code = Integer.toHexString(1 + randomGenerator.nextInt(14));
                        output.append("§").append(color_code).append(word).append(" ");
                    }

                    for (Player p : Bukkit.getOnlinePlayers())
                    {
                        ItemStack heldItem = new ItemStack(Material.CAKE, 1);
                        p.getInventory().setItem(p.getInventory().firstEmpty(), heldItem);
                    }

                    tfBroadcastMessage(output.toString());
                }
                else
                {
                    sender.sendMessage(MSG_NO_PERMS);
                }

                return true;
            }
        }
        catch (Exception ex)
        {
            log.severe("Exception in TotalFreedomMod.onCommand: " + ex.getMessage());
        }

        return false;
    }

    public void tfBroadcastMessage(String message, ChatColor color)
    {
        log.info(message);

        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.sendMessage(color + message);
        }
    }

    public void tfBroadcastMessage(String message)
    {
        log.info(ChatColor.stripColor(message));

        for (Player p : Bukkit.getOnlinePlayers())
        {
            p.sendMessage(message);
        }
    }

    public String implodeStringList(String glue, List<String> pieces)
    {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < pieces.size(); i++)
        {
            if (i != 0)
            {
                output.append(glue);
            }
            output.append(pieces.get(i));
        }
        return output.toString();
    }

    public String formatLocation(Location in_loc)
    {
        return String.format("%s: (%d, %d, %d)",
                in_loc.getWorld().getName(),
                Math.round(in_loc.getX()),
                Math.round(in_loc.getY()),
                Math.round(in_loc.getZ()));
    }

    public boolean isUserSuperadmin(CommandSender user)
    {
        try
        {
            if (!(user instanceof Player))
            {
                return true;
            }

            if (Bukkit.getOnlineMode())
            {
                if (superadmins.contains(user.getName()))
                {
                    return true;
                }
            }

            Player p = (Player) user;
            if (p != null)
            {
                InetSocketAddress ip_address_obj = p.getAddress();
                if (ip_address_obj != null)
                {
                    String user_ip = ip_address_obj.getAddress().toString().replaceAll("/", "").trim();
                    if (user_ip != null && !user_ip.isEmpty())
                    {
                        if (superadmin_ips.contains(user_ip))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            log.severe("Exception in TotalFreedomMod.isUserSuperadmin: " + ex.getMessage());
        }

        return false;
    }

    private void tfHeartbeat()
    {
        for (Player p : Bukkit.getOnlinePlayers())
        {
            TFM_UserInfo playerdata = (TFM_UserInfo) this.userinfo.get(p);
            if (playerdata != null)
            {
                playerdata.resetMsgCount();
                playerdata.resetBlockDestroyCount();
            }
        }

        if (this.autoEntityWipe)
        {
            wipeDropEntities();
        }
    }

    public int wipeDropEntities()
    {
        int removed = 0;
        for (World world : Bukkit.getWorlds())
        {
            for (Entity ent : world.getEntities())
            {
                if (ent instanceof Arrow || ent instanceof TNTPrimed || ent instanceof Item || ent instanceof ExperienceOrb)
                {
                    ent.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
}
