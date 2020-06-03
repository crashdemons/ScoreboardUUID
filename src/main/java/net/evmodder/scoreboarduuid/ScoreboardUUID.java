package net.evmodder.scoreboarduuid;

import com.github.crashdemons.scoreboarduuid.ScoreTransferHelper;
import com.github.crashdemons.scoreboarduuid.ScoreboardUpdateBehavior;
import java.util.Map;
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
        final String currName = evt.getPlayer().getName();
        final String prevName = TagUtil.getPreviousName(evt.getPlayer());
        
        if(transferHelper==null){//this should not occur because we don't register events until after the objects are init
            getLogger().warning("Cannot update scores for player '" + currName + "' - plugin isn't ready yet!");
            return;
        }
        
        if (!prevName.equals(currName)) { // name changed.
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
