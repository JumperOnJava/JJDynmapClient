package io.github.javajump3r.jjdynmap.dynmap.waypoints;

import io.github.javajump3r.jjdynmap.dynmap.DynMapHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

public class ClientWaypoint implements Waypoint {

    @Override
    public int getRawX() {
        return (int)MinecraftClient.getInstance().player.getPos().x;
    }
    public int getRawY(){ return (int)MinecraftClient.getInstance().player.getPos().y;}
    @Override
    public int getRawZ() { return (int)MinecraftClient.getInstance().player.getPos().z;    }

    @Override
    public Identifier getDimension() {
        return DynMapHelper.getCurrentWorld();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getColor() {
        return ColorHelper.Argb.getArgb(255,255,0,0);
    }

    @Override
    public String getListName() {
        return "You";
    }

    @Override
    public boolean showInPlayerList() {
        return true;
    }
}
