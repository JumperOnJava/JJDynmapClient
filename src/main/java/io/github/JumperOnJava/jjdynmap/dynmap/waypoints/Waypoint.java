package io.github.JumperOnJava.jjdynmap.dynmap.waypoints;

import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Waypoint {
    int getRawX();
    int getRawY();
    int getRawZ();
    Identifier getDimension();
    String getName();
    int getColor();
    String getListName();
    boolean showInPlayerList();

    default void RenderWaypointOnScreen(DrawContext context, double centerX, double centerY)
    {
        var profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("point");
        int size = 2;
        context.fill(
                (int)(centerX-(size+2)),
                (int)(centerY-(size+2)),
                (int)(centerX+(size+2)),
                (int)(centerY+(size+2)),
                ColorHelper.Argb.getArgb(255,0,0,0));
        context.fill(
                (int)(centerX-(size+1)),
                (int)(centerY-(size+1)),
                (int)(centerX+(size+1)),
                (int)(centerY+(size+1)),
                ColorHelper.Argb.getArgb(255,255,255,255));
        context.fill(
                (int)(centerX-size),
                (int)(centerY-size),
                (int)(centerX+size),
                (int)(centerY+size),
                this.getColor());
        profiler.pop();
        profiler.push("text");
        var enterY=size*2+1;
        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                OrderedText.styledForwardsVisitedString(this.getName(), Style.EMPTY),
                (int) centerX,
                (int) centerY+enterY,
                ColorHelper.Argb.getArgb(255,255,255,255));
        profiler.pop();
    }
    default String getJson(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    /**
     * @implNote ignores dimension groups, so it returns scale even if dimensions in different groups. You should check if dimensions in same group by using Waypoint.isInSameGroup(dim_a,dim_b)
     * @param dimension
     * @return
     */
    default Vec2f getPosInDimension(Identifier dimension){
        var pos = new Vec2f(this.getRawX(),this.getRawZ());
        pos = pos.multiply(1/getDimensionScale(this.getDimension())).multiply(getDimensionScale(dimension));
        return pos;
    }
    static boolean isInSameGroup(Waypoint a,Waypoint b){
        return isInSameGroup(a.getDimension(),b.getDimension());
    }
    static boolean isInSameGroup(Identifier a,Identifier b){
        for(var group : DimensionGroups){
            if(group.containsKey(a)&&group.containsKey(b)){
                return true;
            }
        }
        return false;
    }
    private float getDimensionScale(Identifier dimension){
        Float scale=null;
        for(var group : DimensionGroups) {
            if(group.containsKey(dimension))
            scale = group.get(dimension);
        }
        if(scale==null)
            return (float) Math.PI;
        return scale;
    }
    List<Map<Identifier,Float>> DimensionGroups = List.of(
            new HashMap<>(Map.of(new Identifier("minecraft","overworld"),1f,new Identifier("minecraft","the_nether"),0.125f)),
            new HashMap<>(Map.of(new Identifier("minecraft","the_end"),1f)));
}
