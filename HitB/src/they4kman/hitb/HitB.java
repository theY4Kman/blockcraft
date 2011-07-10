package they4kman.hitb;

import java.io.File;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.Location;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import org.bukkit.World;

/**
 * BlockCraft for Bukkit
 *
 * @author Zach "theY4Kman" Kanzler
 */
public class HitB extends JavaPlugin
{
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    
    private HitBServer m_Server = null;
    private HitBBlockListener m_BlockListen = null;
   

    public void onEnable()
    {
    }
    
    public void onDisable()
    {
        if (m_Server != null)
            m_Server.stop();
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!label.equals("hitb"))
            return false;
        
        Player player = (Player)sender;
        
        // Update the player
        if (m_Server != null)
            m_Server.setPlayer(player);
        
        if (args.length == 0 && m_Server != null)
            return true;
        
        // Only set a new game board if the first argument is "new"
        if (m_Server != null && !args[0].equals("new"))
            return false;
        
        if (m_Server != null)
            m_Server.stop();
        
        /* Clear space for our stage. */
        World world = player.getWorld();
        Location location = player.getLocation();
        int direction = Math.round((location.getYaw() / 4)) % 4;
        
        int startx = location.getBlockX() - 2;
        int stopx = startx + HitBGame.BOARD_WIDTH + 2;
        
        int startz = location.getBlockZ() - 2;
        int stopz = startz + HitBGame.BOARD_LENGTH + 2;
        
        int starty = location.getBlockY() - 1;
        int stopy = starty + HitBGame.BOARD_HEIGHT + 2;
        
        /* Register block place/destroy handlers */
        PluginManager pm = getServer().getPluginManager();
        if (m_BlockListen == null)
        {
            m_BlockListen = new HitBBlockListener(this);
            
            pm.registerEvent(Event.Type.BLOCK_PLACE, m_BlockListen,
                Priority.Normal, this);
            pm.registerEvent(Event.Type.BLOCK_BREAK, m_BlockListen,
                Priority.Normal, this);
            pm.registerEvent(Event.Type.BLOCK_FROMTO, m_BlockListen,
                Priority.Normal, this);
        }
        
        /* The origin is the start of the building area */
        Location origin = new Location(world, startx+2, starty+1, startz+2);
        
        if (m_Server == null)
            m_Server = new HitBServer(this, origin, player);
        else
            m_Server.setOrigin(origin);
        
        /* Start the server */
        m_Server.start();
        
        return true;
    }
    
    public void clearBounds()
    {
        m_BlockListen.clearBounds();
    }
    
    public void addBounds(short x, short y, short z, short length, short width,
        short height)
    {
        m_BlockListen.addBounds(x, y, z, length, width, height);
    }
    
    public boolean isDebugging(final Player player)
    {
        if (debugees.containsKey(player))
            return debugees.get(player);
        else
            return false;
    }

    public void setDebugging(final Player player, final boolean value)
    {
        debugees.put(player, value);
    }
}

