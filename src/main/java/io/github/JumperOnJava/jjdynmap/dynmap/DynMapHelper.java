package io.github.JumperOnJava.jjdynmap.dynmap;

import io.github.JumperOnJava.autocfg.Configurable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DynMapHelper {
    public static void registerTexture(File texture,Identifier identifier)
    {
        var nativeImage = DynMapHelper.toNativeImage(texture);
        var backedTestTexture = new NativeImageBackedTexture(nativeImage);
        MinecraftClient.getInstance().getTextureManager().registerTexture(identifier,backedTestTexture);
    }
    public static NativeImage toNativeImage(File file){
        try {
            InputStream inputStream = new FileInputStream(file);
            NativeImage nativeImage = NativeImage.read(inputStream);
            inputStream.close();
            return nativeImage;
        }
        catch(Exception e) {
            throw new RuntimeException(String.format("problem registring %s",file.toPath().toString()));
        }
    }
    public static float getPixelsPerBlock(int zoom)
    {
        return (float) Math.pow(2,2-zoom);
    }
    public static int getBlocksPerZoomLevel(int zoom)
    {
        return (int)(128f/getPixelsPerBlock(zoom));
    }

    public static String getJson(String urlString)
    {
        String json = "";
        try {
            URL url = new URL(urlString);
            url.openConnection();
            InputStream is = url.openStream();
            json = new String(is.readAllBytes());
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        return json;
    }
    private static int sessionRandomValue=0;
    public static int sessionRandom()
    {
        if(sessionRandomValue == 0)
            sessionRandomValue = new Random().nextInt();
        return sessionRandomValue;
    }
    public static int getDimensionColor(String dimensionId)
    {
        return (int)colors.get(dimensionId);
    }
    public static boolean isPointInside(int x1, int y1, int x2,int y2, int x, int y)
    {
        if(x > x1 && x < x2 && y > y1 && y < y2)
            return true;
        return false;
    }

    public static HashMap<String,Integer> colors = new HashMap<>();

    static {
        colors.put("midseason_world", ColorHelper.Argb.getArgb(255,115, 207, 50));
        colors.put("midseason_nether", ColorHelper.Argb.getArgb(255,181, 72, 47));
        colors.put("world", ColorHelper.Argb.getArgb(255,69, 255, 146));
        colors.put("world_nether", ColorHelper.Argb.getArgb(255,255, 69, 69));
        colors.put("world_the_end", ColorHelper.Argb.getArgb(255,255, 221, 140));
        colors.put("-some-other-bogus-world-", ColorHelper.Argb.getArgb(255,0,255,255));
    }

    public static String registryKeyToString(Identifier identifier){
        return identifier.toString().replace("minecraft:","");
    }
    public static Identifier getCurrentWorld() {
        var world = MinecraftClient.getInstance().world.getRegistryKey().getValue();
        return world;
    }
    public static String vecToString(Vec3d v)
    {
        return String.format("{%.0f;%.0f;%.0f}",v.x,v.y,v.z);
    }
    public static String vecToString(Vec2f v)
    {
        return String.format("X: %.0f; Z: %.0f",v.x,v.y);
    }

    public static String mcToDynmapWorld(String world) {
        if(world.equals("minecraft:overworld"))
            world = "world";
        if(world.equals("minecraft:the_nether"))
            world = "world_nether";
        if(world.equals("minecraft:the_end"))
            world = "world_the_end";
        world = world.replace(":","-");
        return world;
    }

    public static String dynmapToMcWorld(String world) {
        if(world.equals("world"))
            world = "overworld";
        if(world.equals("world_nether"))
            world = "the_nether";
        if(world.equals("world_the_end"))
            world = "the_end";
        return world;
    }

}
