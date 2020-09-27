package net.evmodder.scoreboarduuid;

import com.github.crashdemons.scoreboarduuid.ScoreTransferHelper;
import com.github.crashdemons.scoreboarduuid.ScoreboardUpdateBehavior;
import com.github.crashdemons.scoreboarduuid.events.PlayerUpdateUsernameEvent;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author EvModder/EvDoc (evdoc at altcraft.net)
 */
public class ScoreboardUUID extends JavaPlugin implements Listener {

    ScoreTransferHelper transferHelper = null;

    @Override
    public void onEnable() {
        Map<String, ScoreboardUpdateBehavior> scoresToUpdate = ConfigUtil.getScoresToUpdate(this);
        if(scoresToUpdate==null){
            getLogger().warning("No uuid-based scores found in config! Disabling plugin");
            this.onDisable();
            return;
        }
        
        transferHelper = new ScoreTransferHelper(this, scoresToUpdate, ConfigUtil.getResetOldScores(this));
        
        getServer().getPluginManager().registerEvents(this, this);
    }



    private void onPlayerJoinSync(PlayerJoinEvent evt) {
        final Player player = evt.getPlayer();
        final String prevName = TagUtil.getPreviousName(player);
        final String currName = player.getName();
        
        if(transferHelper==null){//this should not occur because we don't register events until after the objects are init
            getLogger().warning("Cannot update scores for player '" + currName + "' - plugin isn't ready yet!");
            return;
        }
        
        if (!prevName.equals(currName)) { // name changed.
            
            Bukkit.getServer().getPluginManager().callEvent(new PlayerUpdateUsernameEvent(player, prevName, currName));
            
            boolean success = false;
            try {
                success = transferHelper.updateScores(prevName, currName);
            } catch (IllegalStateException ex) {
                success = false;
            }
            if (!success) {
                getLogger().warning("Encountered error whilst updating scores for player '" + currName + "'!");
                return; // do not reset scoreboard tags if score update failed - attempt again later.
            }
        }
        // reset tags for user.
        TagUtil.updatePreviousName(evt.getPlayer(), prevName, currName);
    }

    @EventHandler
    public void onPlayerJoinAsync(PlayerJoinEvent evt) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> onPlayerJoinSync(evt), 1L);
    }
}
