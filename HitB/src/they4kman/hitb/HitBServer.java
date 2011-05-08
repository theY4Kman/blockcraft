package they4kman.hitb;

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
    
    private static byte[] m_Blocks = new byte[9*36*36];
    
    private static final DyeColor[] m_Colors = {DyeColor.WHITE, DyeColor.ORANGE,
        DyeColor.MAGENTA, DyeColor.YELLOW, DyeColor.SILVER, DyeColor.PURPLE,
        DyeColor.BLUE, DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED};
    
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
                    final int offsetx = 18-HitBGame.BOARD_WIDTH;
                    
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
                        
                        /* Square building area with lengths of BOARD_WIDTH */
                        if (!m_Game.freezeBuild())
                        {
                            World world = m_Player.getWorld();
                            for (int x=0; x<HitBGame.BOARD_WIDTH; x++)
                                for (int y=0; y<9; y++)
                                    for (int z=0; z<HitBGame.BOARD_WIDTH; z++)
                                    {
                                        Block b = world.getBlockAt(m_Origin.getBlockX()+x+offsetx,
                                            m_Origin.getBlockY()+y, m_Origin.getBlockZ()+z);
                                        
                                        if (getDataAtCoord(data, x,y,z) == 0)
                                            b.setType(Material.AIR);
                                        else
                                        {
                                            b.setType(Material.WOOL);
                                            
                                            Wool d = new Wool(Material.WOOL);
                                            d.setColor(m_Colors[z]);
                                        }
                                    }
                        
                            m_Blocks = data;
                        }
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
    
    public byte[] getBlocks()
    {
        return m_Blocks;
    }
    
    static public byte getDataAtCoord(byte[] data, int x, int y, int z)
    {
        if (data == null)
            return 0;
        
        return data[x*9*36+y*36+z];
    }
}
