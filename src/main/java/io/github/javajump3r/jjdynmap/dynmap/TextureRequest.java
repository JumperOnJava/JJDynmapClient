package io.github.javajump3r.jjdynmap.dynmap;

import io.github.javajump3r.autocfg.Configurable;
import io.github.javajump3r.autocfg.CustomCategory;
import io.github.javajumper.lavajumper.LavaJumper;
import io.github.javajumper.lavajumper.common.Feature;
import io.github.javajumper.lavajumper.common.LimitedHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static io.github.javajump3r.jjdynmap.dynmap.DynMapRenderer.DYNMAP_CATEGORY;

public class TextureRequest extends Feature {
    public static Identifier ERROR_TEXTURE = new Identifier("jjhud","textures/gui/dynmap/errortexture.png");

    @Configurable(defaultValue = "Random")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static String prefix="Random";
    @Configurable(defaultValue = "5")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static int maxDownloadAttepts = 5;

    public static int currentDownloads = 0;
    private static HashMap<Integer, TextureRequestInfo> TextureMap = new HashMap<>();
    private int x;
    private int y;
    public final int zoom;
    private String ip;
    private String world;
    private String formatString;
    public final int worldX;
    public final int worldY;
    public static Map<Pair<Integer,Integer>,TextureRequest> cacheMap = new LimitedHashMap<>(256);
    @Configurable(defaultValue = "4")
    @CustomCategory(category = DYNMAP_CATEGORY)
    public static int maxParralelDownloads=1;


    private TextureRequest(String ip, Identifier world, int zoom, int x, int y, int worldX,int worldY) {
        super();
        this.x = x * (int) Math.pow(2, zoom);
        this.y = -y * (int) Math.pow(2, zoom);
        this.zoom = zoom;
        this.world = DynMapHelper.mcToDynmapWorld(world.toString());
        this.ip = ip.hashCode()+"";
        this.worldX=worldX;
        this.worldY=worldY;

        var formatString=DynMapRenderer.mapLink+"/tiles/%s/flat/%d_%d/%s%d_%d.jpg";
        formatString = formatString.replace("//","/");
        formatString = formatString.replace("https:/","https://");
        formatString = formatString.replace("http:/","http://");
        this.formatString = formatString;
    }

    public static TextureRequest WorldSpaceTextureRequest(String ip, Identifier world, int zoom, int x, int y, int xOffset, int yOffset) {

        var blocksPerLevel = DynMapHelper.getBlocksPerZoomLevel(zoom);
        x /= blocksPerLevel;
        y /= blocksPerLevel;
        //x = x / (128/2);
        //y = y / (128/2);

        x += xOffset;
        y += yOffset;
        /*if(Math.abs(x)!=-10)
        return new TextureRequest(ip, world, zoom, x, y,x * blocksPerLevel,y * blocksPerLevel);
            */
        var pair = new Pair<>(x, y);
        var request = cacheMap.get(pair);
        if(request == null){
            request = new TextureRequest(ip, world, zoom, x, y,x * blocksPerLevel,y * blocksPerLevel);
            cacheMap.put(pair,request);
        }
        return request;
    }

    private Path getFile() {
        var directory = FabricLoader.getInstance().getGameDir().resolve(String.format(".jjDynmapCacher/temp/%s/%s/%s/",prefix.equals("Random")?DynMapHelper.sessionRandom():prefix, ip, world));
        directory.toFile().mkdirs();
        var path = directory+String.format("/zoom%d_%d_%d.jpg", zoom, x, y);
        return directory.resolve(path);
    }

    private String getIdentifierString() {
        return String.format("s_%d_w_%s_z_%d_%d_%d", ip.hashCode(), world, zoom, x, y);
    }

    private Identifier getIdentifier() {
        return new Identifier("jjdynmapclient", getIdentifierString());
    }

    private String getDownloadLink() {
        StringBuilder zoomString = new StringBuilder();
        if (zoom != 0) {
            zoomString.append("z".repeat(Math.max(0, zoom)));
            zoomString.append("_");
        }
        int regX = (int) Math.floor(x / 32);
        int regY = (int) Math.floor(y / 32);
        return String.format(formatString, world, regX, regY, zoomString, x, y);
    }

    public Identifier getTexture() {

        //JHudClient.DebugOutput.info(info.status.toString());
        var profiler = MinecraftClient.getInstance().getProfiler();
        profiler.push("GetInfo");
        var info = TextureMap.containsKey(hashCode()) ? TextureMap.get(hashCode()) : new TextureRequestInfo();
        profiler.pop();
        profiler.push(info.status.toString());
        switch (info.status)
        {
            case INIT -> {
                info.status =
                    Files.exists(getFile()) ?
                            TextureRequestInfo.DownloadStatus.DOWNLOADED_NOT_REGISTERED :
                            TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                }
            case NOT_DOWNLOADED -> {
                if(currentDownloads>maxParralelDownloads)
                    break;
                new Thread(() -> {
                    currentDownloads++;
                    info.status= TextureRequestInfo.DownloadStatus.DOWNLOADING;
                    try {
                        LavaJumper.log("downloading",getDownloadLink());
                        InputStream in = new URL(getDownloadLink()).openStream();
                        Files.copy(in, getFile(), StandardCopyOption.REPLACE_EXISTING);
                        info.status= TextureRequestInfo.DownloadStatus.DOWNLOADED_NOT_REGISTERED;
                    } catch (Exception e) {
                        info.status= TextureRequestInfo.DownloadStatus.DOWNLOAD_ERROR;
                    }
                    currentDownloads--;
                }).start();
            }
            case DOWNLOADING -> {
                //HudClient.DebugOutput.info(String.format("Downloading %s",x,y));
            }
            case DOWNLOAD_ERROR ->
            {
                if(info.attempt<maxDownloadAttepts){
                    info.status= TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                    info.attempt++;
                }
            }
            case DOWNLOADED_NOT_REGISTERED -> {
                if(!Files.exists(getFile()))
                {
                    info.status = TextureRequestInfo.DownloadStatus.NOT_DOWNLOADED;
                    break;
                }
                info.identifier = getIdentifier();
                try {
                    DynMapHelper.registerTexture(this.getFile().toFile(), info.identifier);
                }
                catch(Exception e)
                {
                    break;
                }
                info.status= TextureRequestInfo.DownloadStatus.READY;
            }
            case READY -> {
                return TextureMap.get(hashCode()).identifier;
            }
        }

        TextureMap.put(hashCode(), info);
        return ERROR_TEXTURE;

        //JHudClient.DebugOutput.info("error here texture not mapped");
        //return ErrorTexture;
    }

    @Override
    public int hashCode() {
        return getIdentifierString().hashCode();
    }
    private class TextureRequestInfo
    {
        public int attempt;

        public TextureRequestInfo()
        {
            attempt=0;
            status = DownloadStatus.INIT;
        }
        public DownloadStatus status;
        public Identifier identifier;
        public enum DownloadStatus
        {
            INIT,
            NOT_DOWNLOADED,
            DOWNLOADING,
            DOWNLOAD_ERROR,
            DOWNLOADED_NOT_REGISTERED,
            READY,
        }
    }
}
