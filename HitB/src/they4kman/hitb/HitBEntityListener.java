package they4kman.hitb;

import java.util.List;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class HitBEntityListener extends EntityListener
{
    private HitB m_Plugin;
    
    public HitBEntityListener(HitB plugin)
    {
        m_Plugin = plugin;
    }
    
    public void onEntityExplode(EntityExplodeEvent event)
    {
        List<Block> blocks = event.blockList();
        
        for (Iterator<Block> iter=blocks.iterator(); iter.hasNext();)
        {
            Block b = iter.next();
            Location l = b.getLocation();
            
            if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            {
                /* We cannot remove individual blocks from the list, so cancel
                 * the entire event
                 */
                event.setCancelled(true);
                return;
            }
        }
    }
}

