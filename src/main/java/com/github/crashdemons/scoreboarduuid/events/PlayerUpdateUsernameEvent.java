/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.crashdemons.scoreboarduuid.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Event occurring when a player connects and their username has changed from a previously recorded one.
 * @author crashdemons (crashenator at gmail.com)
 */
public class PlayerUpdateUsernameEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String oldName;
    private final String newName;
    
    public PlayerUpdateUsernameEvent(Player p, String oldName, String newName){
        super(p);
        this.oldName=oldName;
        this.newName=newName;
    }
    
    public String getOldUsername(){
        return oldName;
    }
    public String getNewUsername(){
        return newName;
    }
    
    /**
     * Get a list of handlers for the event.
     *
     * @return a list of handlers for the event
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Get a list of handlers for the event.
     *
     * @return a list of handlers for the event
     */
    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
}
