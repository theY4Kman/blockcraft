package they4kman.hitb;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class HitBBlockListener extends BlockListener
{
    private HitB m_Plugin;
    private ArrayList m_Bounds = new ArrayList<short[]>();
    
    public HitBBlockListener(HitB plugin)
    {
        m_Plugin = plugin;
    }
    
    public void clearBounds()
    {
        m_Bounds.clear();
    }
    
    public void addBounds(short x, short y, short z, short length, short width,
        short height)
    {
        short[] bound = new short[] { x, y, z, length, width, height };
        m_Bounds.add(bound);
    }
    
    public boolean pointInBounds(int x, int y, int z)
    {
        for (int i=0; i<m_Bounds.size(); i++)
        {
            short[] bound = (short[])m_Bounds.get(i);
            
            if (bound[0] <= x && x <= bound[0] + bound[3] &&
                bound[1] <= y && y <= bound[1] + bound[5] &&
                bound[2] <= z && z <= bound[2] + bound[4])
                return true;
        }
        
        return false;
    }
    
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockFromTo(BlockFromToEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
};
