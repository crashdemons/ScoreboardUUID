/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.evmodder.scoreboarduuid;

import com.github.crashdemons.scoreboarduuid.ScoreboardUpdateBehavior;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class for retrieving ScoreboardUUID config options
 * @author EvModder
 */
public class ConfigUtil {
    private ConfigUtil(){}//static utility class only
    
    /**
     * Gets the value of the reset-old-scores setting.
     * Note: defaults to 'true'
     * @param plugin the plugin to retrieve the configuration for
     * @return the configured value, or True if not present.
     */
    public static boolean getResetOldScores(JavaPlugin plugin){
        return plugin.getConfig().getBoolean("reset-old-scores", true);
    }
    
    /**
     * Gets the configured list of scores to transfer on name changes with their associated update behavior.
     * @param plugin the plugin to retrieve the configuration for
     * @return a Map of scores configured to be transferred and their associated behaviors. 
     */
    public static Map<String, ScoreboardUpdateBehavior> getScoresToUpdate(JavaPlugin plugin){
        if (!plugin.getConfig().isConfigurationSection("uuid-based-scores")) {
            return null;
            //getLogger().warning("No uuid-based scores found in config! Disabling plugin");
            //this.onDisable();
           // return;
        }
        
        HashMap<String, ScoreboardUpdateBehavior> scoresToUpdate = new HashMap<>();
        ConfigurationSection scoreListSection = plugin.getConfig().getConfigurationSection("uuid-based-scores");//should not be null because we check above AND have a default
        for (String key : scoreListSection.getKeys(false)) {
            String strUpdateBehavior = plugin.getConfig().getString("scoreboard-update-behavior", "OVERWRITE");
            ScoreboardUpdateBehavior updateBehavior;
            try {
                updateBehavior = ScoreboardUpdateBehavior.valueOf(strUpdateBehavior);
            } catch (IllegalArgumentException e) {
                updateBehavior = ScoreboardUpdateBehavior.OVERWRITE;
                plugin.getLogger().warning("Invalid behavior type '" + strUpdateBehavior + "' for score '" + key + "' using " + updateBehavior.name());
            }
            scoresToUpdate.put(key, updateBehavior);
        }
        return scoresToUpdate;
    }
}
