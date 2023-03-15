package io.github.javajump3r.jjdynmap.dynmap;

import com.google.gson.Gson;
import io.github.javajump3r.autocfg.Configurable;
import io.github.javajump3r.autocfg.CustomCategory;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.DynmapPlayerWaypoint;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.Waypoint;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.WaypointStorage;
import io.github.javajumper.lavajumper.LavaJumper;
import io.github.javajumper.lavajumper.common.ToggleableFeature;
import io.github.javajumper.lavajumper.common.actiontext.ActionTextRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static io.github.javajump3r.jjdynmap.dynmap.DynMapRenderer.DYNMAP_CATEGORY;

public class DynmapPlayers extends ToggleableFeature {
    @Configurable(defaultValue = "false")
    @CustomCategory(category = DYNMAP_CATEGORY,name = "playerCoords")
    public static boolean enabled;
    public static DynMapUpdates lastMapUpdate;
    @Configurable(defaultValue = "")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static String playerDataLink = "https://map.chillcraft.online/up/world/midseason_world/1";
    private static DynmapPlayers instance;

    public DynmapPlayers()
    {
        super();
        if(DynmapPlayers.instance!=null)
            return;
        else {
            instance=this;
            HudRenderCallback.EVENT.register(DynmapPlayers::renderInstance);
            new Thread(DynmapPlayers::updateIntance).start();
        }
    }
    public static void updateIntance()
    {
        while (true) {
            try {
                Thread.sleep(1000);
                if(instance!=null)
                    instance.updatePlayers();
            }
            catch (Exception e)
            {
//                throw new RuntimeException(e);
            }
        }
    }
    public static void renderInstance(MatrixStack matrixStack,float delta)
    {
        if(instance!=null)
        instance.render(matrixStack,delta);
    }
    public void updatePlayers() {
        if (MinecraftClient.getInstance().player == null)
            return;
        var mainJson = DynMapHelper.getJson(playerDataLink);
        lastMapUpdate = new Gson().fromJson(mainJson, DynMapUpdates.class);
        lastMapUpdate.players.sort(new PlayerComparator());

        List<Waypoint> waypoints = new LinkedList<>();
        for (var player : lastMapUpdate.players) {
            waypoints.add(new DynmapPlayerWaypoint(player));
        }
        WaypointStorage.getMainInstance().setPlayers(waypoints);
    }
    public void render(MatrixStack matrixStack,float tickDelta)
    {
        if(!enabled)
            return;
        if(MinecraftClient.getInstance().options.debugEnabled)
            return;
        int i=0;
        for(var waypoint : WaypointStorage.getMainInstance().getAllWaypoints())
        {
            if(!waypoint.getName().equals(""))
            try{
                i++;
                if(waypoint.showInPlayerList())
                ActionTextRenderer.renderUpperRight(
                        matrixStack,
                        i,
                        String.format("%s %s at coords %d %d %d",waypoint.getListName(),waypoint.getName(),waypoint.getRawX(),waypoint.getRawY(),waypoint.getRawZ()),
                        waypoint.getColor());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                LavaJumper.log("bruh");
            }
        }
    }
    public class Player{
        public String world;
        public int armor;
        public String name;
        public double x;
        public double y;
        public double health;
        public double z;
        public int sort;
        public String type;
        public String account;
    }
    private class PlayerComparator implements Comparator<Player>{
        @Override
        public int compare(Player a,Player b)
        {
            var apos = new Vec3d(a.x, a.y, a.z);
            var bpos = new Vec3d(b.x, b.y, b.z);
            if(a.world.contains("nether"))
                apos.multiply(8);
            if(a.world.contains("nether"))
                apos.multiply(8);
            Double distance_a = apos.subtract(MinecraftClient.getInstance().player.getPos()).squaredDistanceTo(0,0,0);
            Double distance_b = bpos.subtract(MinecraftClient.getInstance().player.getPos()).squaredDistanceTo(0,0,0);
            return distance_a.compareTo(distance_b);
        }
    }
    class DynMapUpdates {
        public int currentcount;
        public boolean hasStorm;
        public ArrayList<Player> players;
        public boolean isThundering;
        public int confighash;
        public int servertime;
        public ArrayList<Update> updates;
        public long timestamp;
    }
    class Update{
        public String msg;
        public double x;
        public double y;
        public double z;
        public String id;
        public String label;
        public String icon;
        public String set;
        public boolean markup;
        public String desc;
        public String dim;
        public int minzoom;
        public int maxzoom;
        public String ctype;
        public String type;
        public long timestamp;
    }
}