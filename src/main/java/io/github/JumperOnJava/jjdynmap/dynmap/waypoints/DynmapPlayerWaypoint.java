package io.github.JumperOnJava.jjdynmap.dynmap.waypoints;

import io.github.JumperOnJava.jjdynmap.dynmap.DynMapHelper;
import io.github.JumperOnJava.jjdynmap.dynmap.DynmapPlayers;
import net.minecraft.util.Identifier;

public class DynmapPlayerWaypoint implements Waypoint {
     DynmapPlayers.Player player;
    public DynmapPlayerWaypoint(DynmapPlayers.Player player)
    {
        this.player = player;
    }
    @Override
    public int getRawX() {
        return (int)player.x;
    }

    @Override
    public int getRawY() {
        return (int)player.y;
    }

    @Override
    public int getRawZ() {
        return (int)player.z;
    }

    @Override
    public Identifier getDimension() {
        return new Identifier(DynMapHelper.dynmapToMcWorld(player.world));
    }

    @Override
    public String getName() {
        return player.name;
    }

    @Override
    public int getColor() {
        return DynMapHelper.getDimensionColor(player.world);
    }

    @Override
    public String getListName() {
        return "Player";
    }

    @Override
    public boolean showInPlayerList() {
        return true;
    }
}
