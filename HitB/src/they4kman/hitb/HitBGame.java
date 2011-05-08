package they4kman.hitb;

import org.javaworld.JarResources;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.DyeColor;

public class HitBGame
{
    private HitB m_Plugin;
    private HitBServer m_Server;
    private Location m_Origin;
    private Player m_Player;
    
    private JarResources m_Jar;
    private ArrayList<String> m_Cutouts = new ArrayList<String>();
    private boolean[][] m_CurCutout = null;
    
    private int m_WallOffset = 0;
    private int m_RoundTime = -5*20;
    private int m_CurRound = 1;
    private boolean m_RedrawWall = false;
    private boolean m_FreezeBuild = true;
    
    private boolean m_Halt = false;
    
    public static final int BOARD_LENGTH = 32;
    public static final int BOARD_WIDTH = 12;
    public static final int BOARD_HEIGHT = 9;
    
    public HitBGame(HitB plugin, HitBServer server, Player player, Location origin)
    {
        m_Plugin = plugin;
        m_Server = server;
        m_Player = player;
        m_Origin = origin;
        
        m_Jar = new JarResources("plugins/hitb.jar");
    }
    
    public boolean freezeBuild()
    {
        return m_FreezeBuild;
    }
    
    /* Returns nextstep */
    private int nextRound()
    {
        /* Clear last wall */
        World world = m_Player.getWorld();
        for (int y=0; y<9; y++)
            for (int x=0; x<12; x++)
            {
                Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                    m_Origin.getBlockY()+y, m_Origin.getBlockZ()+BOARD_LENGTH-1-m_WallOffset);
                b.setType(Material.AIR);
            }
        
        /* Reset wall */
        m_WallOffset = 0;
        m_RedrawWall = true;
        /* Reset cutout */
        m_CurCutout = null;
        
        /* Next round */
        m_CurRound++;
        /* Reset round time to 5-second countdown */
        m_RoundTime = -5*20;
        
        /* 1 second delay between rounds */
        return 20;
    }
    
    private Runnable _mainloop = new Runnable()
    {
        public void run()
        {
            mainloop();
        }
    };
    
    public void mainloop()
    {
        int nextstep = 10;
        
        if (m_CurCutout == null)
        {
            /* Grab the cutout filenames from the jar */
            if (m_Cutouts.size() == 0)
            {
                Set entries = m_Jar.getEntries();
                for (Object obj : entries)
                {
                    String entry = (String)obj;
                    if (!entry.startsWith("cutouts/"))
                        continue;
                    
                    if (m_Jar.getResource(entry) == null)
                        continue;
                    
                    m_Cutouts.add(entry);
                }
            }
            
            int idx = (int)(Math.random()*m_Cutouts.size());
            String cutout = m_Cutouts.get(idx);
            m_Cutouts.remove(idx);
            
            m_CurCutout = loadCutout(cutout);
            m_RedrawWall = true;
        }
        
        if (m_RoundTime < 0)
        {
            nextstep = 20;
            m_Player.sendMessage(String.format("Round %d starts in %d seconds",
                m_CurRound, -m_RoundTime/20));
            m_RoundTime += nextstep;
        }
        else if (m_RoundTime < (BOARD_LENGTH/2)*20)
        {
            
            /* Clear current wall */
            World world = m_Player.getWorld();
            for (int y=0; y<9; y++)
                for (int x=0; x<12; x++)
                {
                    Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                        m_Origin.getBlockY()+y, m_Origin.getBlockZ()+BOARD_LENGTH-1-m_WallOffset);
                    b.setType(Material.AIR);
                }
            
            
            m_WallOffset++;
            m_RedrawWall = true;
            
            if (m_RoundTime < 10*20)
                m_FreezeBuild = false;
            else
            {
                m_FreezeBuild = true;
                byte[] blocks = m_Server.getBlocks();
                for (int y=0; y<9; y++)
                    for (int x=0; x<12; x++)
                    {
                        if (m_Server == null)
                            System.out.println("MOFUCKINNULL");
                        
                        if ((m_Server.getDataAtCoord(blocks, x,y,BOARD_LENGTH-1-m_WallOffset) == 1 ? true : false)
                                != m_CurCutout[8-y][x])
                        {
                            /* Lose */
                            m_Player.sendMessage("You lost this round :(");
                            nextstep = nextRound();
                        }
                    }
            }
            
            m_RoundTime += nextstep;
        }
        else
            nextstep = nextRound();
        
        if (m_RedrawWall && m_CurCutout != null)
        {
            World world = m_Player.getWorld();
            for (int y=0; y<9; y++)
                for (int x=0; x<12; x++)
                {
                    Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                        m_Origin.getBlockY()+y, m_Origin.getBlockZ()+BOARD_LENGTH-1-m_WallOffset);
                    
                    if (b == null)
                        System.out.println("NULLSHUTFUCKTITS!");
                    
                    if (m_CurCutout[8-y][x])
                    {
                        b.setType(Material.WOOL);
                        Wool d = new Wool(Material.WOOL);
                        d.setColor(DyeColor.PURPLE);
                        b.setData(d.getData());
                    }
                    else
                        b.setType(Material.GLASS);
                }
        }
        
        /* 20 ticks per second */
        m_Plugin.getServer().getScheduler().scheduleAsyncDelayedTask(m_Plugin,
            _mainloop, nextstep);
    }
    
    protected boolean[][] loadCutout(String filename)
    {
        byte[] blob = m_Jar.getResource(filename);
        if (blob == null)
            return null;
        
        boolean[][] board = new boolean[9][12];
        String board_str = new String(blob);
        
        CharacterIterator it = new StringCharacterIterator(board_str); 
        int pos = 0;
        for (char ch=it.first(); ch != CharacterIterator.DONE && pos < 9*12; ch=it.next())
        {
            if (ch == '.')
                board[pos/12][pos%12] = false;
            else if (ch == '#')
                board[pos/12][pos%12] = true;
            else if (ch == '\n')
                continue;
            else
                return null;
            
            pos++;
        }
        
        return board;
    }
};
