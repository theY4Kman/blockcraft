package they4kman.hitb;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class HitBBlockListener extends BlockListener
{
    private HitB plugin;
    
    private int startx;
    private int stopx;
    
    private int starty;
    private int stopy;
    
    private int startz;
    private int stopz;
    
    public HitBBlockListener(HitB plugin, int startx, int stopx, int starty,
        int stopy, int startz, int stopz)
    {
        this.plugin = plugin;
        
        this.startx = startx;
        this.stopx = stopx;
        
        this.starty = starty;
        this.stopy = stopy;
        
        this.startz = startz;
        this.stopz = stopz;
    }
    
    public void setCoords(int startx, int stopx, int starty, int stopy,
        int startz, int stopz)
    {
        this.startx = startx;
        this.stopx = stopx;
        
        this.starty = starty;
        this.stopy = stopy;
        
        this.startz = startz;
        this.stopz = stopz;
    }
    
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (startx <= l.getBlockX() && l.getBlockX() < stopx
            && starty <= l.getBlockY() && l.getBlockY() < stopy
            && startz <= l.getBlockZ() && l.getBlockZ() < stopz)
            event.setCancelled(true);
    }
    
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block b = event.getBlock();
        Location l = b.getLocation();
        
        if (startx <= l.getBlockX() && l.getBlockX() < stopx
            && starty <= l.getBlockY() && l.getBlockY() < stopy
            && startz <= l.getBlockZ() && l.getBlockZ() < stopz)
            event.setCancelled(true);
    }
    
    public void onBlockFromTo(BlockFromToEvent event)
    {
        Block b = event.getToBlock();
        Location l = b.getLocation();
        
        if (startx <= l.getBlockX() && l.getBlockX() < stopx
            && starty <= l.getBlockY() && l.getBlockY() < stopy
            && startz <= l.getBlockZ() && l.getBlockZ() < stopz)
            event.setCancelled(true);
    }
};
