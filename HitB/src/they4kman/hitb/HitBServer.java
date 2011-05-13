package they4kman.hitb;

import java.util.Arrays;

import org.zeromq.ZMQ;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.DyeColor;

public class HitBServer
{
    private final HitB m_Plugin;
    private int m_TaskID = -1;
    private HitBGame m_Game = null;
    
    private Location m_Origin;
    private Player m_Player;
    
    private ZMQ.Context m_Context;
    private ZMQ.Socket m_Socket;
    
    /**
     * origin: The ground center of the blockcraft stage (y=player's feet)
     * player: The player who initiated blockcraft
     */
    public HitBServer(HitB instance, Location origin, Player player)
    {
        m_Plugin = instance;
        
        m_Origin = origin;
        m_Player = player;
        
        try
        {
            m_Context = ZMQ.context(1);
            m_Socket = m_Context.socket(ZMQ.PAIR);
        }
        catch (UnsatisfiedLinkError ex)
        {}
        
        m_Socket.bind("tcp://*:8134");
    }
    
    public void setOrigin(Location origin)
    {
        m_Origin = origin;
    }
    
    public void setPlayer(Player player)
    {
        m_Player = player;
    }
    
    /* Begins the network event loop */
    public void start()
    {
        m_Game = new HitBGame(m_Plugin, this, m_Player, m_Origin);
        
        m_TaskID = m_Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(m_Plugin,
            new Runnable()
            {
                public void run()
                {
                    /* Start the game main loop */
                    m_Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(m_Plugin,
                        new Runnable()
                        {
                            public void run()
                            {
                                m_Game.mainloop();
                            }
                        });
                    
                    /* 0MQ loop */
                    while (true)
                    {
                        final byte[] data = m_Socket.recv(0);
                        
                        m_Game.updateBlocks(data);
                    }
                }
            });
    }
    
    /* Stops the network loop */
    public void stop()
    {
        if (m_TaskID == -1)
            return;
        m_Plugin.getServer().getScheduler().cancelTask(m_TaskID);
    }
}
