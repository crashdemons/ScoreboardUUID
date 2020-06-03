/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.evmodder.scoreboarduuid;

import org.bukkit.entity.Player;

/**
 * Utility class for handling scoreboard tags tracking player username history
 * @author EvModder
 */
public class TagUtil {
    private TagUtil(){}//static utility class only
    
    /**
     * Retrieves the previously recorded username for the player provided
     * @param player the player to check
     * @return the player's previous name, or their current name if none is found.
     */
    public static String getPreviousName(Player player) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith("prev_name_")) {
                return tag.substring(10);
            }
        }
        return player.getName();
    }
    
    /**
     * Updates tracking information for a players 'previous' name.
     * This should be used when you are finished updating all information for the user, or an old name doesn't exist.
     * @param player the player to update
     * @param prevName the name the user had before
     * @param currName the current name of the user to record before the next change.
     */
    public static void updatePreviousName(Player player, String prevName, String currName){
        player.removeScoreboardTag("prev_name_" + prevName);
        player.addScoreboardTag("prev_name_" + currName);
    }
}
