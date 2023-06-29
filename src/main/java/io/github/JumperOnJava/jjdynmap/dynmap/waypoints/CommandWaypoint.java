package io.github.JumperOnJava.jjdynmap.dynmap.waypoints;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class CommandWaypoint implements Waypoint {

    public int x,y,z;
    public String name;
    public String dimension;
    public boolean renderedInList;
    public int color;

    public CommandWaypoint(Vec3d pos, String name, int color, String dimension){
        this.x=(int)pos.x;
        this.y=(int)pos.y;
        this.z=(int)pos.z;
        this.name=name;
        this.color=color;
        this.dimension=dimension;
    }

    @Override
    public int getRawX() {
        return (int)x;
    }

    @Override
    public int getRawY() {
        return (int)y;
    }

    @Override
    public int getRawZ() {
        return (int)z;
    }

    @Override
    public Identifier getDimension() {
        return new Identifier(dimension);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public String getListName() {
        return "Waypoint";
    }

    @Override
    public boolean showInPlayerList() {
        return renderedInList;
    }

    public int hashCode(){
        return name.hashCode();
    }
}
