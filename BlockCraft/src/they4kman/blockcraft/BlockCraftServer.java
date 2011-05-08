package they4kman.blockcraft;

import org.zeromq.ZMQ;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.material.Wool;
import org.bukkit.DyeColor;

public class BlockCraftServer
{
    private final BlockCraft m_Plugin;
    
    private final Location m_Origin;
    private final Player m_Player;
    
    private ZMQ.Context m_Context;
    private ZMQ.Socket m_Socket;
    
    private static byte[] m_Blocks;
    
    private static final DyeColor[] m_Colors = {DyeColor.WHITE, DyeColor.ORANGE,
        DyeColor.MAGENTA, DyeColor.YELLOW, DyeColor.SILVER, DyeColor.PURPLE,
        DyeColor.BLUE, DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED};
    
    /**
     * center: The ground center of the blockcraft stage (y=player's feet)
     * player: The player who initiated blockcraft
     */
    public BlockCraftServer(BlockCraft instance, Location origin, Player player)
    {
        m_Plugin = instance;
        
        m_Origin = origin;
        m_Player = player;
        
        m_Context = ZMQ.context(1);
        m_Socket = m_Context.socket(ZMQ.PAIR);
        
        m_Socket.bind("tcp://*:8134");
        
        m_Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(m_Plugin,
            new Runnable()
            {
                public void run()
                {
                    /* 0MQ loop */
                    while (true)
                    {
                        final byte[] data = m_Socket.recv(0);
                        
                        World world = m_Player.getWorld();
                        for (int x=0; x<36; x++)
                            for (int y=0; y<9; y++)
                                for (int z=0; z<36; z++)
                                {
                                    Block b = world.getBlockAt(m_Origin.getBlockX()+x,
                                        m_Origin.getBlockY()+y, m_Origin.getBlockZ()+z);
                                    
                                    if (getDataAtCoord(data, x,y,z) == 0)
                                        b.setType(Material.AIR);
                                    else
                                    {
                                        b.setType(Material.WOOL);
                                        
                                        Wool d = new Wool(Material.WOOL);
                                        d.setColor(m_Colors[z]);
                                        b.setData(d.getData());
                                    }
                                }
                    }
                }
            });
    }
    
    static private byte getDataAtCoord(byte[] data, int x, int y, int z)
    {
        return data[x*9*36+y*36+z];
    }
}
