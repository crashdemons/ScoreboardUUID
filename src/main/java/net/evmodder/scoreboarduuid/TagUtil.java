/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.evmodder.scoreboarduuid;

import org.bukkit.entity.Player;

/**
 *
 * @author EvModder
 */
public class TagUtil {
    private TagUtil(){}//static utility class only
    public static String getPreviousName(Player player) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith("prev_name_")) {
                return tag.substring(10);
            }
        }
        return player.getName();
    }
    
    public static void updatePreviousName(Player player, String prevName, String currName){
        player.removeScoreboardTag("prev_name_" + prevName);
        player.addScoreboardTag("prev_name_" + currName);
    }
}
