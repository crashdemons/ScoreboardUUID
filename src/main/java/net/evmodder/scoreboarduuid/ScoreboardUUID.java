package net.evmodder.scoreboarduuid;

import com.github.crashdemons.scoreboarduuid.ScoreTransferHelper;
import com.github.crashdemons.scoreboarduuid.ScoreboardUpdateBehavior;
import java.util.HashMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
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
        HashMap<String, ScoreboardUpdateBehavior> scoresToUpdate = new HashMap<>();
        boolean resetOldScores;
        if (!getConfig().isConfigurationSection("uuid-based-scores")) {
            getLogger().warning("No uuid-based scores found in config! Disabling plugin");
            this.onDisable();
            return;
        }
        resetOldScores = getConfig().getBoolean("reset-old-scores", true);

        ConfigurationSection scoreListSection = getConfig().getConfigurationSection("uuid-based-scores");
        for (String key : scoreListSection.getKeys(false)) {
            String strUpdateBehavior = getConfig().getString("scoreboard-update-behavior", "OVERWRITE");
            ScoreboardUpdateBehavior updateBehavior;
            try {
                updateBehavior = ScoreboardUpdateBehavior.valueOf(strUpdateBehavior);
            } catch (IllegalArgumentException e) {
                updateBehavior = ScoreboardUpdateBehavior.OVERWRITE;
                getLogger().warning("Invalid behavior type '" + strUpdateBehavior + "' for score '" + key + "' using " + updateBehavior.name());
            }
            scoresToUpdate.put(key, updateBehavior);
        }

        transferHelper = new ScoreTransferHelper(this, scoresToUpdate, resetOldScores);
        
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
