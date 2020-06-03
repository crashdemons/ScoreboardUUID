/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.scoreboarduuid;

import com.github.crashdemons.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Class object that assists in transferring scores between two usernames.
 * @author crashdemons (crashenator at gmail.com)
 */
public class ScoreTransferHelper {
    private final JavaPlugin plugin;
    private final HashMap<String, ScoreboardUpdateBehavior> scoresToUpdate;
    private final boolean resetOldScores;//may need to change final if you want reload capability.
    
    /**
     * Constructs a new instance of the transfer helper
     * @param parentPlugin the bukkit plugin requesting support
     * @param scoresToUpdate a map of named scoreboard objectives and their associated update behaviors
     * @param resetOldScores whether to reset all scores of the user(s) the scores are being transferred from.
     */
    public ScoreTransferHelper(JavaPlugin parentPlugin, HashMap<String, ScoreboardUpdateBehavior> scoresToUpdate, boolean resetOldScores){
        plugin=parentPlugin;
        this.scoresToUpdate = scoresToUpdate;
        this.resetOldScores = resetOldScores;
    }
    
    private Logger getLogger(){
        return plugin.getLogger();
    }
    
    
    boolean updateScore(Objective obj, String username, int scoreValue, ScoreboardUpdateBehavior updateBehavior) {
        Score newScoreObject = obj.getScore(username);

        int newScoreValue;
        switch (updateBehavior) {
            case SAFE_MOVE:
                if (newScoreObject.isScoreSet()) {
                    getLogger().severe("Failed to update score '" + obj.getName() + "' for user '" + username + "' - entry already exists!");
                    return false;
                }
            //else, intentional fallthrough - overwrite unset scores.
            case OVERWRITE:
                newScoreValue = scoreValue;
                break;
            case ADD:
                newScoreValue = scoreValue + (newScoreObject.isScoreSet() ? newScoreObject.getScore() : 0);
                break;
            default:
                getLogger().severe("Encountered invalid behavior type " + updateBehavior + " while updating score: " + obj.getName());
                newScoreValue = -1;
                return false;
        }
        //if(newScoreObject.isScoreSet() && newScoreObject.getScore() == newScoreValue) return false; // no change occurred.
        newScoreObject.setScore(newScoreValue);
        return true;
    }

    /**
     * Transfer configured scores from one username to another
     * @param oldName the name to transfer scores from
     * @param newName the name to transfer scores to
     * @return whether scores could be transferred successfully.
     * @throws IllegalStateException thrown when the scoreboard manager is not available (world is not loaded).
     */
    public boolean updateScores(String oldName, String newName) {
        getLogger().info("Updating scoreboard of '" + oldName + "' to '" + newName + "'");

        final ScoreboardManager sm = plugin.getServer().getScoreboardManager();
        if (sm == null) {
            throw new IllegalStateException("World has not loaded yet - this is a bug!");
        }

        final Scoreboard sb = sm.getMainScoreboard();
        final HashMap<Objective, Pair<Integer, ScoreboardUpdateBehavior>> scores = new HashMap<>();

        // collect scores for old username.
        for (Map.Entry<String, ScoreboardUpdateBehavior> entry : scoresToUpdate.entrySet()) {
            Objective obj = sb.getObjective(entry.getKey());
            if (obj == null) {
                getLogger().warning("Scoreboard Objective " + entry.getKey() + " doesn't exist!");
                continue;
            }
            Score score = obj.getScore(oldName);
            if (!score.isScoreSet()) {
                continue;
            }
            scores.put(obj, new Pair<>(score.getScore(), entry.getValue()));
        }

        boolean moveSuccess = true;
        // transfer collected scores to new username.
        for (Map.Entry<Objective, Pair<Integer, ScoreboardUpdateBehavior>> entry : scores.entrySet()) {
            moveSuccess &= updateScore(entry.getKey(), newName, entry.getValue().a, entry.getValue().b);
        }

        // clear scores for old username.
        if (resetOldScores && moveSuccess) {
            HashMap<Objective, Integer> scoresToKeep = new HashMap<>();
            for (Objective obj : sb.getObjectives()) {
                if (scoresToUpdate.containsKey(obj.getName()) || !obj.getScore(oldName).isScoreSet()) {
                    continue;
                }
                scoresToKeep.put(obj, obj.getScore(oldName).getScore());
            }
            sb.resetScores(oldName);
            for (Map.Entry<Objective, Integer> entry : scoresToKeep.entrySet()) {
                entry.getKey().getScore(oldName).setScore(entry.getValue());
            }
        }
        return moveSuccess;
    }
}
