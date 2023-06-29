package io.github.JumperOnJava.jjdynmap.dynmap;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.JumperOnJava.autocfg.Configurable;
import io.github.JumperOnJava.autocfg.CustomCategory;
import io.github.JumperOnJava.jjdynmap.dynmap.waypoints.Waypoint;
import io.github.JumperOnJava.jjdynmap.dynmap.waypoints.WaypointStorage;
import io.github.JumperOnJava.lavajumper.common.ToggleableFeature;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

public class DynMapRenderer extends ToggleableFeature {

    public static final String DYNMAP_CATEGORY = "DynmapIntegration";
    @Configurable(defaultValue = "false")
    @CustomCategory(category = DYNMAP_CATEGORY, name = "minimap")
    public static boolean enabled;
    @Configurable(defaultValue = "false")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static boolean onlyOnTab = false;
    @Configurable(defaultValue = "")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static String mapLink = "https://map.chillcraft.online";

    Identifier testId = new Identifier("textures/gui/dynmap/testtexture.png");;
    DynMapHelper provider;
    @Configurable(defaultValue = "5",minValue = 0,maxValue = 5)
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static int zoom = 5;
    public static double cellsizeConst=0.5;

    @Configurable(defaultValue = "2",minValue = 1d/8,maxValue = 8,interval = 1d/8)
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static double textScale = .5f;

    //@Configurable(defaultValue = "4",minValue = 1,maxValue = 16,interval = 1)
    //@CustomCategory(category = DYNMAP_CATEGORY)
    private static int sqrRadius=4;

    public DynMapRenderer()
    {
        super();
        HudRenderCallback.EVENT.register(this::render);
    }
    MinecraftClient client = MinecraftClient.getInstance();
    public void render(DrawContext context, float tickDelta)
    {
        var profiler = client.getProfiler();
        profiler.push("minimap");
        {
            profiler.push("prepare");

                if(!enabled)
                {
                    profiler.pop();
                    profiler.pop();
                    return;
                }
                if(onlyOnTab)
                    if(!client.options.playerListKey.isPressed())
                    {
                        profiler.pop();
                        profiler.pop();
                        return;
                    }
                if(!FabricLoader.getInstance().isDevelopmentEnvironment())
                    if(client.options.debugEnabled)
                        return;
                var scalegui = client.options.getGuiScale().getValue();
                var scale = 0.5;
                var fscale=scale*scalegui;
                int cellsize = (int)(fscale*64);
                var ip=mapLink;
                var world= DynMapHelper.getCurrentWorld();

                var playerPos = client.player.getPos();
                var playerRot = client.getInstance().player.getYaw();

                int top=8;
                int left=9;
                int width = (int)((cellsize*sqrRadius-2)*(cellsizeConst));
                int height = (int)((cellsize*sqrRadius-2)*(cellsizeConst));
                //DrawableHelper.fill(matrixStack,width,height,width+cellsize,height+cellsize, ColorHelper.Argb.getArgb(255,0,255,0));
                final int down = top + height;
                final int right = left + width;

                var bpc = DynMapHelper.getBlocksPerZoomLevel(zoom);
                var bpp = DynMapHelper.getPixelsPerBlock(zoom);
                var playerx = (playerPos.x%bpc)/cellsize*bpp*32;
                var playery = (playerPos.z%bpc)/cellsize*bpp*32;

            profiler.pop();

            profiler.push("sides");
                renderSides(context,top,left,width,height-1);
            profiler.pop();

            profiler.push("main_render");

            context.enableScissor(left-1,top,right-1,down+1);
            for(int x=-1;x<sqrRadius+1;x++)
            {
                for(int y=-1;y<sqrRadius+2;y++) {
                    profiler.push("request");

                        profiler.push("prepare");
                            TextureRequest req = TextureRequest.worldSpaceTextureRequest(ip,world,zoom,(int)playerPos.x,(int)playerPos.z,x-sqrRadius/2,y-sqrRadius/2);
                        profiler.pop();

                        profiler.push("getTexture");
                            var texture = req.getTexture();
                            profiler.pop();
                        profiler.pop();

                    profiler.pop();

                    profiler.push("draw");
                    context.drawTexture(texture,
                            (int)((x*cellsize*fscale +left-playerx*fscale)*(cellsizeConst)),
                            (int)((y*cellsize*fscale +top-playery*fscale)*(cellsizeConst)),
                            0,0,
                            (int)((cellsize*fscale)*(cellsizeConst)),
                            (int)((cellsize*fscale)*(cellsizeConst)),
                            (int)((cellsize*fscale)*(cellsizeConst)),
                            (int)((cellsize*fscale)*(cellsizeConst)));
                    profiler.pop();
                }
            }
            profiler.pop();

            profiler.push("arrow");
            {
                List<Vec2f> arrow = new LinkedList<>();
                for(float i=0;i<3;i+=.5)
                {
                    arrow.add(new Vec2f(i+4,0));
                }
                for(int i=0;i<arrow.size();i++)
                {
                    var x = arrow.get(i).x*2;
                    var y = arrow.get(i).y*2;
                    var ang = playerRot;
                    ang += 90;
                    //ang = ((int)(ang/zoom))*zoom;
                    ang *= 0.0174533;
                    var finX = x * cos(ang) - y * sin(ang);
                    var finY = x * sin(ang) + y * cos(ang);
                    context.fill(
                            (int)((left+width/2-1+finX)),
                            (int)((top+height/2+finY)),
                            (int)((left+width/2+1+finX)),
                            (int)((top+height/2+2+finY)),
                            ColorHelper.Argb.getArgb(255,255,128,128));
                }
                context.fill(
                        (int)((left+width/2-1)),
                        (int)((top+height/2)),
                        (int)((left+width/2+1)),
                        (int)((top+height/2+1)),
                        ColorHelper.Argb.getArgb(255,0,255,0));
                RenderSystem.disableScissor();
            }
            profiler.pop();
            profiler.push("waypoints");
            {
                profiler.push("render");
                var currentDimension = DynMapHelper.getCurrentWorld();
                profiler.push("getWaypoints");
                var allWaypoints = WaypointStorage.getMainInstance().getAllWaypoints();
                profiler.pop();
                for(var waypoint : allWaypoints)
                {
                    profiler.push("prepare");
                    if(!Waypoint.isInSameGroup(waypoint.getDimension(),currentDimension))
                    {
                        profiler.pop();
                        continue;
                    }
                    context.getMatrices().push();
                    context.getMatrices().scale((float) textScale,(float) textScale,(float) textScale);

                    var pos = waypoint.getPosInDimension(currentDimension);
                    var coords = new Vec3d(pos.x,0,pos.y).subtract(playerPos);

                    coords = coords.multiply(Math.pow(2,-zoom+1));
                    coords = coords.multiply(cellsizeConst);
                    if(!DynMapHelper.isPointInside(-width/2,-height/2,width/2,height/2,(int) coords.x,(int) coords.z))
                    {
                        var ang = atan2(coords.x,coords.y);

                        var vec = new Vec3d(coords.x,0,coords.z);
                        vec = vec.multiply(1/max(abs(vec.x),abs(vec.z)));
                        vec = vec.multiply(width/2);


                        coords = vec;
                    }
                    var centerX = left+width/2 + coords.x;
                    centerX*=1/textScale;
                    var centerY = top+height/2 + coords.z;
                    centerY*=1/textScale;

                    profiler.pop();
                    profiler.push("render");
                    waypoint.RenderWaypointOnScreen(context,centerX,centerY);
                    context.getMatrices().pop();
                    profiler.pop();
                }
                profiler.pop();
            }
            profiler.pop();
        }
        profiler.pop();
    }
    public void renderSides(DrawContext context,int x,int y,int width,int height)
    {
        y-=1;
        height+=1;
        context.fill(x-3, y-3, x + width+3, y + height+3, 0xFF000000);
        context.fill(x-2, y-2, x + width+2, y + height+2, 0xFFFFFFFF);
    }



}
