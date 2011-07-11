package they4kman.hitb;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class HitBBlockListener extends BlockListener
{
    private HitB m_Plugin;
    
    public HitBBlockListener(HitB plugin)
    {
        m_Plugin = plugin;
    }
    
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockFromTo(BlockFromToEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setCancelled(true);
    }
    
    public void onBlockCanBuild(BlockCanBuildEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (m_Plugin.pointInBounds(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
            event.setBuildable(false);
    }
};
