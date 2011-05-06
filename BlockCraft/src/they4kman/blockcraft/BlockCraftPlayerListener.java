package they4kman.blockcraft;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Zach "theY4Kman" Kanzler
 */
public class BlockCraftPlayerListener extends PlayerListener {
    private final BlockCraft plugin;
    private BlockCraftServer m_Server = null;

    public BlockCraftPlayerListener(BlockCraft instance) {
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
            player.sendMessage("BlockCraft already in progress.");
            return;
        }
        
        String[] sects = event.getMessage().split(" +", 2);
        String cmd = sects[0];
        String[] args = (sects.length > 1 ? sects[1].split(" +") : new String[0]);

        if (!cmd.equals("blockcraft"))
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
        
        int stopx = location.getBlockX() + 18;
        int stopy = location.getBlockY() + 9;
        int stopz = location.getBlockZ() + 18;
        
        int startx = location.getBlockX()-18;
        int startz = location.getBlockZ()-18;
        int starty = location.getBlockY();
        
        switch (direction)
        {
        case 0:
            startz += 18;
            stopz += 18;
            break;
        
        case 1:
            startx -= 18;
            stopx -= 18;
            break;
        
        case 2:
            startz -= 18;
            stopz -= 18;
            break;
        
        case 3:
            startx += 18;
            stopx += 18;
        };
        
        /* 36x36x7, lwh, x left/right, y up/down, z forward/back */
        
        for (int x=startx; x<stopx; x++)
            for (int z=startz; z<stopz; z++)
            {
                for (int y=starty; y<stopy; y++)
                {
                    Block b = world.getBlockAt(x, y, z);
                    b.setType(Material.AIR);
                }
                
                /* Build a roof and floor of glass as well */
                Block b = world.getBlockAt(x, stopy, z);
                b.setType(Material.GLASS);
                b = world.getBlockAt(x, starty-1, z);
                b.setType(Material.GLASS);
            }
        
        Location origin = new Location(world, startx, starty, startz);
        m_Server = new BlockCraftServer(plugin, origin, player);

        /* Cancel the command, preventing other plug-ins from seeing it */
        event.setCancelled(true);
    }                 

    //Insert Player related code here
}

