package they4kman.BADASS;

import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Server;
import org.bukkit.World;

/**
 * Block Addition and Deletion After Suitable Scheme
 * B.A.D.A.S.S.
 *
 * @author Zach "theY4Kman" Kanzler
 */
public class BADASS extends JavaPlugin
{
    private BADASSServer m_Server = null;

    public void onEnable()
    {
        m_Server = new BADASSServer(this);
        m_Server.start();
    }
    
    public void onDisable()
    {
        if (m_Server != null)
            m_Server.stop();
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = (Player)sender;
        
        if (label.equals("startsave"))
        {
            player.getInventory().addItem(new ItemStack());
            
            return true;
        }
        else if (label.equals("savedesign"))
            return true;
        else if (label.equals("savelegos"))
            return true;
        else
            return false;
    }
}

