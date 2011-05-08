package they4kman.hitb;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.material.Wool;
import org.bukkit.DyeColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;

/**
 * Handle events for all Player related events
 * @author Zach "theY4Kman" Kanzler
 */
public class HitBPlayerListener extends PlayerListener
{
    private final HitB plugin;
    private HitBServer m_Server = null;
    private HitBBlockListener m_BlockListen = null;

    public HitBPlayerListener(HitB instance) {
        plugin = instance;
    }
    
    public void onPlayerChat(PlayerChatEvent event)
    {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        if (!player.isOnline())
            return;
        
        if (m_Server != null)
        {
            m_Server.stop();
            m_Server.setPlayer(player);
        }
        
        String[] sects = event.getMessage().split(" +", 2);
        String cmd = sects[0];
        String[] args = (sects.length > 1 ? sects[1].split(" +") : new String[0]);

        if (!cmd.equals("hitb"))
            return;
        
        /* Clear space for our stage. */
        World world = player.getWorld();
        Location location = player.getLocation();
        int direction = Math.round((location.getYaw() / 4)) % 4;
        
        /* Directions:
         * 0: +z
         * 1: -x
         * 2: -z
         * 3: +x
         */
        
        int startx = location.getBlockX() - 2;
        int stopx = startx + HitBGame.BOARD_WIDTH + 2;
        
        int startz = location.getBlockZ() - 2;
        int stopz = startz + HitBGame.BOARD_LENGTH + 2;
        
        int starty = location.getBlockY() - 1;
        int stopy = starty + HitBGame.BOARD_HEIGHT + 2;
        
        for (int x=startx; x<stopx; x++)
            for (int z=startz; z<stopz; z++)
            {
                /* Ground */
                Block b = world.getBlockAt(x, starty, z);
                if (x == startx+1 || x == stopx-2)
                {
                    b.setType(Material.WOOL);
                    Wool d = new Wool(Material.WOOL);
                    d.setColor(DyeColor.PURPLE);
                    b.setData(d.getData());
                }
                else if (x == startx || x == stopx-1)
                    b.setType(Material.GRASS);
                else if (startx+1 < x && x < stopx-2 && z == startz+HitBGame.BOARD_WIDTH+1)
                {
                    b.setType(Material.WOOL);
                    Wool d = new Wool(Material.WOOL);
                    d.setColor(DyeColor.GREEN);
                    b.setData(d.getData());
                }
                else
                {
                    b.setType(Material.WOOL);
                    Wool d = new Wool(Material.WOOL);
                    d.setColor(DyeColor.BLACK);
                    b.setData(d.getData());
                }
                
                /* Clear hall */
                for (int y=starty+1; y<stopy; y++)
                {
                    b = world.getBlockAt(x, y, z);
                    b.setType(Material.AIR);
                }
                
                /* Build a roof of glass */
                b = world.getBlockAt(x, stopy, z);
                b.setType(Material.GLASS);
                
                /* Place fences along the border */
                if (x == startx || x == stopx-1 || z == startz)
                {
                    b = world.getBlockAt(x, starty+1, z);
                    b.setType(Material.FENCE);
                }
                
                /* Build game wall */
                if (z == stopz-1)
                {
                    for (int y=starty+1; y<stopy; y++)
                    {
                        b = world.getBlockAt(x, y, z);
                        b.setType(Material.GLASS);
                    }
                }
            }
        
        /* 0x8: top half, 0x4: open, 0x1: SE hinge, 0x2: SW hinge, 0x3: NW hinge */
        /* Add a door */
        Block b = world.getBlockAt(startx+1, starty+1, startz);
        b.setType(Material.WOODEN_DOOR);
        b.setData((byte)(b.getData() | 0x2));
        /* Top half */
        b = world.getBlockAt(startx+1, starty+2, startz);
        b.setType(Material.WOODEN_DOOR);
        b.setData((byte)(b.getData() | 0x8 | 0x2));
        
        /* Register block place/destroy handlers */
        PluginManager pm = plugin.getServer().getPluginManager();
        if (m_BlockListen == null)
        {
            m_BlockListen = new HitBBlockListener(plugin, startx, stopx, starty,
                stopy, startz, stopz);
            
            pm.registerEvent(Event.Type.BLOCK_PLACE, m_BlockListen,
                Priority.Normal, plugin);
            pm.registerEvent(Event.Type.BLOCK_BREAK, m_BlockListen,
                Priority.Normal, plugin);
            pm.registerEvent(Event.Type.BLOCK_FROMTO, m_BlockListen,
                Priority.Normal, plugin);
        }
        else
            m_BlockListen.setCoords(startx, stopx, starty, stopy, startz,
                stopz);
        
        /* The origin is the start of the building area */
        Location origin = new Location(world, startx+2, starty+1, startz+2);
        
        if (m_Server == null)
            m_Server = new HitBServer(plugin, origin, player);
        else
            m_Server.setOrigin(origin);
        
        /* Start the server */
        m_Server.start();

        /* Cancel the command, preventing other plug-ins from seeing the cmd */
        event.setCancelled(true);
    }                 

    //Insert Player related code here
}

