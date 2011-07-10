package they4kman.hitb;

import org.javaworld.JarResources;
import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.material.Wool;
import org.bukkit.DyeColor;
import org.bukkit.event.block.BlockIgniteEvent;

public class HitBGame
{
    private HitB m_Plugin;
    private HitBServer m_Server;
    private Location m_Origin;
    private Player m_Player;
    
    private JarResources m_Jar;
    private ArrayList<String> m_Cutouts = new ArrayList<String>();
    private boolean[][] m_CurCutout = null;
    private byte[][][] m_Blocks = new byte[36][9][36];
    
    private int m_WallOffset = 0;
    private int m_RoundTime = -5*20;
    private int m_CurRound = 1;
    private boolean m_RedrawWall = false;
    private boolean m_FreezeBuild = true;
    private boolean m_LostRound = false;
    private boolean m_BlockFound = false;
    
    private int m_Score = 0;
    
    private boolean m_Halt = false;
    
    public static final int BOARD_LENGTH = 32;
    public static final int BOARD_WIDTH = 12;
    public static final int BOARD_HEIGHT = 9;
    
    private static final DyeColor[] m_Colors = {
        DyeColor.BLUE,
        DyeColor.BLUE,
        DyeColor.BLUE,
        DyeColor.RED,
        DyeColor.GREEN,
        DyeColor.PURPLE,
        DyeColor.BLUE,
        DyeColor.BROWN,
        DyeColor.GREEN,
        DyeColor.RED
    };
    
    public HitBGame(HitB plugin, HitBServer server, Player player, Location origin)
    {
        m_Plugin = plugin;
        m_Server = server;
        m_Player = player;
        m_Origin = origin;
        
        m_Jar = new JarResources("plugins/hitb.jar");
    }
    
    private void clearBlocks()
    {
        for (int x=0; x<36; x++)
            for (int y=0; y<9; y++)
                for (int z=0; z<36; z++)
                    m_Blocks[x][y][z] = 0;
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
        /* Reset block found for cutout matching */
        m_BlockFound = false;
        
        /* Next round */
        m_CurRound++;
        /* Reset round time to 5-second countdown */
        m_RoundTime = -5*20;
        
        /* Can't lose a round if you haven't played it! */
        m_LostRound = false;
        
        /* Tell the player their current score */
        m_Player.sendMessage(String.format("Score: %d", m_Score));
        
        /* 1 second delay between rounds */
        return 20;
    }
    
    public void updateBlocks(final byte[] data)
    {
        if (m_FreezeBuild)
            return;
        
        final int offsetxz = 18-HitBGame.BOARD_WIDTH/2;
        final byte[] datacopy = Arrays.copyOf(data, data.length);
        
        /* Make sure we modify the world only in the main thread */
        m_Plugin.getServer().getScheduler().scheduleSyncDelayedTask(m_Plugin, new Runnable()
        {
            public void run()
            {
                World world = m_Player.getWorld();
                /* Square building area with lengths of BOARD_WIDTH */
                for (int x=2; x<HitBGame.BOARD_WIDTH-2; x++)
                    for (int y=0; y<HitBGame.BOARD_HEIGHT; y++)
                        for (int z=2; z<HitBGame.BOARD_WIDTH-2; z++)
                        {
                            Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                                m_Origin.getBlockY()+y, m_Origin.getBlockZ()+z);
                            
                            m_Blocks[offsetxz+x][y][offsetxz+z] = datacopy[(offsetxz+x)*36*9+y*36+(offsetxz+z)];
                            
                            /* Place the blocks in the build area */
                            if (datacopy[(offsetxz+x)*36*9+y*36+(offsetxz+z)] == 1)
                            {
                                b.setType(Material.WOOL);
                                
                                Wool d = new Wool(Material.WOOL);
                                d.setColor(m_Colors[y]);
                                b.setData(d.getData());
                            }
                            else
                                b.setType(Material.AIR);
                        }
            }
        });
    }
    
    private void groundSweepLine(final DyeColor color, final int ticks, final int delay)
    {
        final Server srv = m_Plugin.getServer();
        
        srv.getScheduler().scheduleSyncDelayedTask(m_Plugin, new Runnable()
        {
            private int m_Offset = 0;
            private boolean m_Fix = false;
            
            public void run()
            {
                World world = m_Player.getWorld();
                
                /* Reset last line */
                if (m_Offset > 0)
                {
                    DyeColor c = DyeColor.BLACK;
                    if (m_Offset == BOARD_WIDTH+1)
                        c = DyeColor.BLUE;
                    
                    for (int x=0; x<BOARD_WIDTH-2; x++)
                    {
                        Block b = world.getBlockAt(m_Origin.getBlockX()+x,
                            m_Origin.getBlockY()-1, m_Origin.getBlockZ()+m_Offset-1);
                        b.setType(Material.WOOL);
                        Wool d = new Wool(Material.WOOL, b.getData());
                        d.setColor(c);
                        b.setData(d.getData());
                    }
                }
                
                if (m_Offset >= BOARD_LENGTH)
                    return;
                
                for (int x=0; x<BOARD_WIDTH-2; x++)
                {
                    Block b = world.getBlockAt(m_Origin.getBlockX()+x,
                        m_Origin.getBlockY()-1, m_Origin.getBlockZ()+m_Offset);
                    b.setType(Material.WOOL);
                    Wool d = new Wool(Material.WOOL);
                    d.setColor(color);
                    b.setData(d.getData());
                }
                
                m_Offset++;
                srv.getScheduler().scheduleSyncDelayedTask(m_Plugin, this, ticks);
            }
        }, delay);
    }
    
    private void sweepBlinkAnim(final DyeColor color)
    {
        final Server srv = m_Plugin.getServer();
        final World world = m_Player.getWorld();
        
        srv.getScheduler().scheduleSyncDelayedTask(m_Plugin, new Runnable()
        {
            private int m_Offset = 0;
            /* 0: cascade, 1: blink, 2: done */
            private int m_State = 0;
            
            public void run()
            {
                /* Sweep into build square */
                if (m_State == 0)
                {
                    if (m_Offset < BOARD_WIDTH/2+1)
                    {
                        for (int x=0; x<BOARD_WIDTH-2; x++)
                        {
                            Block b = world.getBlockAt(m_Origin.getBlockX()+x,
                                m_Origin.getBlockY()-1,
                                m_Origin.getBlockZ()+m_Offset-2);
                            b.setType(Material.WOOL);
                            Wool d = new Wool(Material.WOOL);
                            d.setColor(color);
                            b.setData(d.getData());
                            
                            b = world.getBlockAt(m_Origin.getBlockX()+x,
                                m_Origin.getBlockY()-1,
                                m_Origin.getBlockZ()+BOARD_WIDTH-m_Offset-2);
                            b.setType(Material.WOOL);
                            d = new Wool(Material.WOOL);
                            d.setColor(color);
                            b.setData(d.getData());
                        }
                        
                        m_Offset++;
                    }
                    else
                    {
                        m_State = 1;
                        m_Offset = 0;
                    }
                }
                
                /* Blink three times */
                if (m_State == 1)
                {
                    if (m_Offset < 6)
                    {
                        for (int x=0; x<BOARD_WIDTH-2; x++)
                            for (int z=-1; z<BOARD_WIDTH; z++)
                            {
                                Block b = world.getBlockAt(
                                    m_Origin.getBlockX()+x,
                                    m_Origin.getBlockY()-1,
                                    m_Origin.getBlockZ()+z-1);
                                b.setType(Material.WOOL);
                                Wool d = new Wool(Material.WOOL);
                                d.setColor((m_Offset % 2 == 0) ? color : DyeColor.BLACK);
                                b.setData(d.getData());
                            }
                        
                        m_Offset++;
                    }
                    else
                        m_State = 2;
                }
                
                if (m_State < 2)
                    srv.getScheduler().scheduleSyncDelayedTask(m_Plugin, this, 2);
            }
        });
    }
    
    private void beginLossAnimation()
    {
        sweepBlinkAnim(DyeColor.RED);
    }
    
    private void beginWinAnimation()
    {
        sweepBlinkAnim(DyeColor.GREEN);
    }
    
    private Runnable _mainloop = new Runnable()
    {
        public void run()
        {
            //mainloop();////////////////////////////////////////
        }
    };
    
    public void mainloop()
    {
        final int offsetxz = 18-HitBGame.BOARD_WIDTH/2;
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
        
        /* Pre-round countdown */
        if (m_RoundTime < 0)
        {
            /* Pre-round countdown, 1 tick per second */
            nextstep = 20;
            m_Player.sendMessage(String.format("Round %d starts in %d second%s",
                m_CurRound, -m_RoundTime/20, (-m_RoundTime/20) == 1 ? "" : "s"));
            m_RoundTime += nextstep;
        }
        
        /* Game time! */
        else if (m_RoundTime < (BOARD_LENGTH)*20/2 && m_CurCutout != null && !m_LostRound)
        {
            /* Clear current wall */
            World world = m_Player.getWorld();
            for (int y=0; y<9; y++)
                for (int x=0; x<12; x++)
                {
                    Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                        m_Origin.getBlockY()+y, m_Origin.getBlockZ()+BOARD_LENGTH-1-m_WallOffset);
                    try
                    {
                        if (b != null && (BOARD_LENGTH-m_WallOffset>=BOARD_WIDTH
                                || m_Blocks[offsetxz+x][y][offsetxz+BOARD_LENGTH-m_WallOffset-1] == 0))
                            b.setType(Material.AIR);
                    }
                    catch (ArrayIndexOutOfBoundsException e)
                    {}
                }
            
            /* Move the wall ahead and mark it for redraw */
            m_WallOffset++;
            m_RedrawWall = true;
            
            /* Before hitting the freeze line */
            if (m_RoundTime < 9*20)
                m_FreezeBuild = false;
            
            /* After the freeze line */
            else
            {
                m_FreezeBuild = true;
                boolean design_matched = true;
                boolean block_found = false;
                for (int y=0; y<BOARD_HEIGHT && !m_LostRound; y++)
                    for (int x=2; x<BOARD_WIDTH && !m_LostRound; x++)
                    {
                        /* If the block exists in the build area */
                        if (m_Blocks[offsetxz+x][y][BOARD_LENGTH-m_WallOffset+offsetxz] == 1)
                        {
                            block_found = true;
                            
                            /* But not in the cutout */
                            if (!m_CurCutout[y][x])
                            {
                                /* Lose */
                                m_LostRound = true;
                                break;
                            }
                        }
                        
                        /* Block does not exist in build area */
                        else
                        {
                            /* But does exist in the cutout */
                            if (m_CurCutout[y][x])
                                design_matched = false;
                        }
                    }
                
                /* If the cutout wasn't matched, but there were blocks found */
                if (!design_matched && block_found)
                {
                    /* Lose */
                    m_LostRound = true;
                }
                
                if (block_found)
                    m_BlockFound = block_found;
            }
            
            if (!m_LostRound)
                m_RoundTime += nextstep;
        }
        else
        {
            if (!m_LostRound && m_BlockFound)
            {
                m_Player.sendMessage("You won this round!");
                m_Score += 10;
                beginWinAnimation();
            }
            else
            {
                /* No blocks were found when doing cutout matching */
                if (!m_BlockFound)
                    m_Player.sendMessage("You didn't use any blocks before the freeze line.");
                else
                    m_Player.sendMessage("You didn't match all of the purple design!");
                
                m_Player.sendMessage("You lost this round :(");
                m_LostRound = true;
            }
            
            /* Loss animation */
            if (m_LostRound)
                beginLossAnimation();
            
            nextstep = nextRound();
        }
        
        /* Redraw the wall if there is a cutout loaded */
        if (m_RedrawWall && m_CurCutout != null)
        {
            World world = m_Player.getWorld();
            for (int y=0; y<9; y++)
                for (int x=0; x<12; x++)
                {
                    Block b = world.getBlockAt(m_Origin.getBlockX()+x-1,
                        m_Origin.getBlockY()+y, m_Origin.getBlockZ()+BOARD_LENGTH-1-m_WallOffset);
                    
                    if (b == null)
                    {
                        System.err.println("Block is somehow null!");
                        continue;
                    }
                    
                    try
                    {
                        if (m_CurCutout[y][x])
                        {
                            if (m_Blocks[offsetxz+x][y][offsetxz+BOARD_LENGTH-m_WallOffset-1] == 0)
                                b.setType(Material.AIR);
                        }
                        else
                        {
                            b.setType(Material.WOOL);
                            Wool d = new Wool(Material.WOOL);
                            d.setColor(DyeColor.YELLOW);
                            b.setData(d.getData());
                        }
                    }
                    
                    /* These inextricably appear, but do no harm */
                    catch (ArrayIndexOutOfBoundsException e)
                    {}
                    catch (IndexOutOfBoundsException e)
                    {}
                }
        }
        
        /* 20 ticks per second */
        m_Plugin.getServer().getScheduler().scheduleSyncDelayedTask(m_Plugin,
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
                board[8-pos/12][pos%12] = false;
            else if (ch == '#')
                board[8-pos/12][pos%12] = true;
            else if (ch == '\n')
                continue;
            else
                return null;
            
            pos++;
        }
        
        return board;
    }
};
