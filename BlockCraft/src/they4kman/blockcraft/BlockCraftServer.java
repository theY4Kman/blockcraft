package they4kman.blockcraft;

import org.zeromq.ZMQ;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class BlockCraftServer
{
    private final BlockCraft m_Plugin;
    
    private final Location m_Origin;
    private final Player m_Player;
    
    private ZMQ.Context m_Context;
    private ZMQ.Socket m_Socket;
    
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
                        
                        /* Run the place blocks task */
                        m_Plugin.getServer().getScheduler().scheduleSyncDelayedTask(
                            m_Plugin, new Runnable()
                            {
                                public void run()
                                {
                                    /* xyz, yzx, yxz, zxy, zyx */
                                    World world = m_Player.getWorld();
                                    for (int x=0; x<36; x++)
                                        for (int y=0; y<9; y++)
                                            for (int z=0; z<36; z++)
                                            {
                                                Block b = world.getBlockAt(m_Origin.getBlockX()+x,
                                                    m_Origin.getBlockY()+y, m_Origin.getBlockZ()+z);
                                                
                                                b.setType(data[x*9*36+y*36+z] == 1 ? Material.GRASS : Material.AIR);
                                            }
                                }
                            });
                    }
                }
            });
    }
}
