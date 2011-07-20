package they4kman.BADASS;

import java.util.Arrays;
import java.nio.ByteBuffer;

import org.zeromq.ZMQ;
import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.Location;
import org.bukkit.DyeColor;
import org.bukkit.scheduler.BukkitScheduler;

public class BADASSServer
{
    private final BADASS m_Plugin;
    private int m_TaskID = -1;
    
    private Location m_Origin = null;
    private Player m_Player = null;
    
    private ZMQ.Context m_Context = null;
    private ZMQ.Socket m_Socket = null;
    
    public final char COMMAND_BLOCK = 'b';
    public final char COMMAND_PLAYERMESSAGE = 'p';
    
    public HitBServer(HitB instance)
    {
        m_Plugin = instance;
        
        try
        {
            if (m_Context == null)
                m_Context = ZMQ.context(1);
            m_Socket = m_Context.socket(ZMQ.PULL);
        }
        catch (UnsatisfiedLinkError ex)
        {
            System.err.println("Could not link ZeroMQ socket!");
            return;
        }
        
        m_Socket.bind("tcp://*:8134");
    }
    
    public void setPlayer(Player player)
    {
        m_Player = player;
        m_Origin = player.getLocation();
    }
    
    /* Begins the network event loop */
    public void start()
    {
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
                         */
                        
                        // Where we are in the data array
                        int dp = 0;
                        
                        byte[] num_commands_bytes = new byte[4];
                        System.arraycopy(data, 0, num_commands_bytes, 0, 4);
                        dp += 4;
                        
                        int num_commands = byteArrayToInt(num_commands_bytes);
                        
                        for (int i=0; i<num_commands; i++)
                        {
                            if (dp >= data.length)
                            {
                                System.err.println("Command data pointer exceeded packet size! " +
                                    "The HitB Python script sent a malformed packet.");
                                System.err.println("Data pointer: " + dp + "  Packet size: " + data.length);
                                System.err.println("Number of commands: " + num_commands + "  Processed: " + i);
                                break;
                            }
                            
                            char cmd_type = (char)data[dp++];
                            
                            switch (cmd_type)
                            {
                            case COMMAND_BLOCK:
                                {
                                    byte[] buf = new byte[2];
                                    short[] offset = new short[3];
                                    
                                    // Don't add blocks if no player is set
                                    if (m_Player == null)
                                        continue;
                                    
                                    // Grab the player's current location before adding blocks
                                    m_Origin = player.getLocation();
                                    
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
