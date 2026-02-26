package io.github.sst.remake.event.impl.game.player;

import io.github.sst.remake.event.Event;

public class SafeWalkEvent extends Event {
    public Situation situation = Situation.DEFAULT;
    public boolean onEdge;

    public SafeWalkEvent(boolean onEdge) {
        this.onEdge = onEdge;
    }

    public void setSafe(boolean safe) {
        this.situation = safe ? Situation.SAFE : Situation.PLAYER;
    }

    public enum Situation {
        DEFAULT, PLAYER, SAFE
    }
}