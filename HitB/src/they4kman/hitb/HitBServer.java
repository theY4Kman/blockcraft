package they4kman.hitb;

import java.util.Arrays;
import java.nio.ByteBuffer;

import org.zeromq.ZMQ;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.DyeColor;
import org.bukkit.scheduler.BukkitScheduler;

public class HitBServer
{
    private final HitB m_Plugin;
    private int m_TaskID = -1;
    private HitBGame m_Game = null;
    
    private Location m_Origin;
    private Player m_Player;
    
    private ZMQ.Context m_Context = null;
    private ZMQ.Socket m_Socket = null;
    
    public final char COMMAND_BLOCK = 'b';
    public final char COMMAND_PLAYERMESSAGE = 'p';
    public final char COMMAND_COLORWOOL = 'w';
    public final char COMMAND_SETBOUNDS = 's';
    public final char COMMAND_CLEARBOUNDS = 'c';
    
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
            if (m_Context == null)
                m_Context = ZMQ.context(1);
            m_Socket = m_Context.socket(ZMQ.PAIR);
        }
        catch (UnsatisfiedLinkError ex)
        {
            System.err.println("Could not link ZeroMQ socket!");
            return;
        }
        
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
        //m_Game = new HitBGame(m_Plugin, this, m_Player, m_Origin);
        
        if (m_Socket == null)
        {
            System.err.println("Can't start server with null socket.");
            return;
        }
        
        final BukkitScheduler scheduler = m_Plugin.getServer().getScheduler();
        m_TaskID = scheduler.scheduleAsyncDelayedTask(m_Plugin,
            new Runnable()
            {
                public void run()
                {
                    /* Start the game main loop */
                    /*m_Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(m_Plugin,
                        new Runnable()
                        {
                            public void run()
                            {
                                m_Game.mainloop();
                            }
                        });*/
                    
                    /* 0MQ loop */
                    while (true)
                    {
                        final byte[] data = m_Socket.recv(0);
                        
                        /**
                         * Every packet begins with a 4-byte integer represent-
                         * ing the number of commands. Every command begins with
                         * a single character identifying the type of command.
                         * 
                         * 'b' - Set block command. Begins with three shorts
                         *       x, y, z representing the offset from the origin
                         *       of the block to set. The last short is the
                         *       Material type ID to set the block.
                         *       TOTAL BYTES: 8
                         *
                         * 'p' - Send player message. Consists of a C string of
                         *       variable length, ending with a null.
                         *       TOTAL BYTES: variable
                         * 
                         * 'w' - Set wool colour command. Begins with three
                         *       shorts x, y, z representing the offset from the
                         *       origin of the block to set. The last byte is
                         *       the DyeColor ID to set the wool. This command
                         *       automatically sets the block type to wool.
                         *       TOTAL BYTES: 7
                         * 
                         * 's' - Set bounds for the block listener. Begins with
                         *       three shorts x, y, z representing the origin
                         *       for the bounding box. Lastly, three shorts
                         *       length, width, height representing the dimens-
                         *       ions for the bounding box.
                         *       TOTAL BYTES: 12
                         *
                         * 'c' - Clear bounds for the block listener. Resets all
                         *       the bounds set with the 's' command. Empty
                         *       command.
                         */
                        
                        // Where we are in the data array
                        int dp = 0;
                        
                        byte[] num_commands_bytes = new byte[4];
                        System.arraycopy(data, 0, num_commands_bytes, 0, 4);
                        dp += 4;
                        
                        int num_commands = byteArrayToInt(num_commands_bytes);
                        
                        for (int i=0; i<num_commands; i++)
                        {
                            char cmd_type = (char)data[dp++];
                            
                            switch (cmd_type)
                            {
                            case COMMAND_BLOCK:
                                {
                                    byte[] buf = new byte[2];
                                    short[] offset = new short[3];
                                    
                                    for (int j=0; j<3; j++)
                                    {
                                        System.arraycopy(data, dp, buf, 0, 2);
                                        dp += 2;
                                        offset[j] = byteArrayToShort(buf);
                                    }
                                    
                                    System.arraycopy(data, dp, buf, 0, 2);
                                    dp += 2;
                                    
                                    final short type = byteArrayToShort(buf);
                                    final short[] foffset = new short[] {
                                        offset[0], offset[1], offset[2] };
                                    
                                    scheduler.scheduleSyncDelayedTask(m_Plugin,
                                        new Runnable()
                                        {
                                            public void run()
                                            {
                                                World world = m_Player.getWorld();
                                                Block b = world.getBlockAt(
                                                    m_Origin.getBlockX() + foffset[0],
                                                    m_Origin.getBlockY() + foffset[1],
                                                    m_Origin.getBlockZ() + foffset[2]);
                                                
                                                b.setTypeId((int)type);
                                            }
                                        });
                                    
                                    break;
                                }
                            
                            case COMMAND_PLAYERMESSAGE:
                                {
                                    int null_byte = dp;
                                    while (data[null_byte++] != '\0');
                                    
                                    final ByteBuffer bytestr = ByteBuffer.allocate(null_byte-dp-1);
                                    bytestr.put(data, dp, null_byte-dp-1);
                                    
                                    dp = null_byte;
                                    
                                    scheduler.scheduleSyncDelayedTask(m_Plugin,
                                        new Runnable()
                                        {
                                            public void run()
                                            {
                                                String message = new String(bytestr.array());
                                                m_Player.sendMessage(message);
                                            }
                                        });
                                    
                                    break;
                                }
                            
                            case COMMAND_COLORWOOL:
                                {
                                    byte[] buf = new byte[2];
                                    short[] offset = new short[3];
                                    
                                    for (int j=0; j<3; j++)
                                    {
                                        System.arraycopy(data, dp, buf, 0, 2);
                                        dp += 2;
                                        offset[j] = byteArrayToShort(buf);
                                    }
                                    
                                    byte dye = data[dp];
                                    dp++;
                                    
                                    final DyeColor color = DyeColor.getByData(dye);
                                    final short[] foffset = new short[] {
                                        offset[0], offset[1], offset[2] };
                                    
                                    scheduler.scheduleSyncDelayedTask(m_Plugin,
                                        new Runnable()
                                        {
                                            public void run()
                                            {
                                                World world = m_Player.getWorld();
                                                Block b = world.getBlockAt(
                                                    m_Origin.getBlockX() + foffset[0],
                                                    m_Origin.getBlockY() + foffset[1],
                                                    m_Origin.getBlockZ() + foffset[2]);
                                                b.setType(Material.WOOL);
                                                
                                                Wool d = new Wool(Material.WOOL);
                                                d.setColor(color);
                                                b.setData(d.getData());
                                            }
                                        });
                                    
                                    break;
                                }
                            
                            case COMMAND_SETBOUNDS:
                                {
                                    byte[] buf = new byte[2];
                                    short[] offset = new short[3];
                                    short[] dimensions = new short[3];
                                    
                                    // Origin
                                    for (int j=0; j<3; j++)
                                    {
                                        System.arraycopy(data, dp, buf, 0, 2);
                                        dp += 2;
                                        offset[j] = byteArrayToShort(buf);
                                    }
                                    
                                    // Dimensions
                                    for (int j=0; j<3; j++)
                                    {
                                        System.arraycopy(data, dp, buf, 0, 2);
                                        dp += 2;
                                        dimensions[j] = byteArrayToShort(buf);
                                    }
                                    
                                    m_Plugin.addBounds(
                                        (short)(m_Origin.getBlockX() + offset[0]),
                                        (short)(m_Origin.getBlockY() + offset[1]),
                                        (short)(m_Origin.getBlockZ() + offset[2]),
                                        dimensions[0], dimensions[1],
                                        dimensions[2]);
                                    
                                    break;
                                }
                            
                            case COMMAND_CLEARBOUNDS:
                                {
                                    m_Plugin.clearBounds();
                                    
                                    break;
                                }
                            
                            default:
                                {
                                    System.err.println("Unknown command type '" + cmd_type +
                                        "' passed to Hole in the Blocks.");
                                    break;
                                }
                            }
                        }
                    }
                }
            });
    }
    
    /* Stops the network loop */
    public void stop()
    {
        if (m_TaskID != -1)
            m_Plugin.getServer().getScheduler().cancelTask(m_TaskID);
        
        if (m_Socket != null)
            m_Socket.close();
    }

    public static int byteArrayToInt(byte[] b)
    {
        ByteBuffer bb = ByteBuffer.wrap(b);
        return bb.getInt();
    }
    
    public static short byteArrayToShort(byte[] b)
    {
        ByteBuffer bb = ByteBuffer.wrap(b);
        return bb.getShort();
    }
}
