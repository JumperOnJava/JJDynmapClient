package io.github.javajump3r.jjdynmap.dynmap.waypoints;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.javajump3r.jjdynmap.dynmap.DynMapHelper;
import io.github.javajumper.lavajumper.common.FileReadWrite;
import io.github.javajumper.lavajumper.common.actiontext.ActionTextRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WaypointStorage {

    private static WaypointStorage mainInstance;
    private List<Waypoint> players = new LinkedList<>();
    private Map<String, CommandWaypoint> commandWaypoints = new HashMap<>();
    private Waypoint clientWaypoint = new ClientWaypoint();
    private static File waypointsFile;
    public static WaypointStorage getMainInstance() {
        if(mainInstance==null)
        {
            waypointsFile = FabricLoader.getInstance().getConfigDir().resolve("JJDynmapWaypoints.json").toFile();
            mainInstance = new WaypointStorage();
            ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
                var builder = literal("jjwaypoint");
                builder
                        .then(literal("add")
                                .then(argument("name",StringArgumentType.word())
                                        .then(literal("at")
                                                .then(argument("pos", BlockPosArgumentType.blockPos()).executes(mainInstance::addWaypointCommand)
                                                        .then(argument("dimension", DimensionArgumentType.dimension()).executes(mainInstance::addWaypointCommand))
                                                        .then(argument("dimension_name",StringArgumentType.word()).executes(mainInstance::addWaypointCommand))
                                                )
                                        ).executes(mainInstance::addWaypointCommand)
                                )
                        )
                        .then(literal("edit")
                                .then(argument("name",StringArgumentType.word())
                                    .then(literal("pos")
                                            .then(argument("coords",BlockPosArgumentType.blockPos()).executes(mainInstance::editPoint))
                                    )
                                    .then(literal("color")
                                            .then(argument("rgb", ColorArgumentType.color()).executes(mainInstance::editPoint)))
                                    .then(literal("world")
                                            .then(argument("dimension", DimensionArgumentType.dimension()).executes(mainInstance::editPoint))
                                            .then(argument("dimension_name",StringArgumentType.word()).executes(mainInstance::editPoint))
                                    )
                                    .then(literal("list_display")
                                            .then(argument("show", BoolArgumentType.bool()).executes(mainInstance::editPoint)))
                                        .then(literal("info").executes(mainInstance::printWaypoint))
                                )
                        )
                        .then(literal("remove")
                                .then(argument("name",StringArgumentType.word()).executes(mainInstance::removeWaypoint)));
                dispatcher.register(builder);
            }));
            mainInstance.restoreWaypoints();
        }
        return mainInstance;
    }

    private int printWaypoint(CommandContext<FabricClientCommandSource> commandContext) {
            var name = commandContext.getArgument("name",String.class);
            ActionTextRenderer.sendChatMessage(commandWaypoints.get(name).getJson());
            return 0;
    }

    private int editPoint(CommandContext<FabricClientCommandSource> commandContext) {
        restoreWaypoints();
        var name = getArgumentOrNull(commandContext,"name",String.class);
        var coords = getArgumentOrNull(commandContext,"coords", PosArgument.class);
        var color = getArgumentOrNull(commandContext,"color", Formatting.class);
        Boolean show = getArgumentOrNull(commandContext,"show",Boolean.class);
        var dimension = getDimensionFromContext(commandContext);
        CommandWaypoint editPoint = commandWaypoints.get(name);
        if(editPoint==null)
            throw new RuntimeException(String.format("Point %s does not exist or null",name));
        if(coords != null){
            var pos = getPosArgumentCoords(coords,commandContext);
            editPoint.x=(int) pos.x;
            editPoint.y=(int) pos.y;
            editPoint.z=(int) pos.z;
        }
        if(color != null){
            editPoint.color = color.getColorIndex();
        }
        if(show != null){
            editPoint.renderedInList = show;
        }
        if(dimension!=null){
            editPoint.dimension=dimension;
        }
        saveWaypoints();
        return 0;
    }

    private void restoreWaypoints(){
        try{
            var read = FileReadWrite.read(waypointsFile);
            if(read.equals("")){
                read="{}";
            }
            commandWaypoints = new Gson().fromJson(read, TypeToken.getParameterized(HashMap.class,String.class,CommandWaypoint.class).getType());
        }
        catch (Exception e){
            throw e;
        }
    }
    private void saveWaypoints(){
        FileReadWrite.write(waypointsFile,new Gson().toJson(commandWaypoints));
    }
    public void setPlayers(List<Waypoint> list)
    {
        this.players = list;
    }
    private WaypointStorage() {
        if(mainInstance==null)
            mainInstance=this;
        else
            return;
        restoreWaypoints();

    }

    private int removeWaypoint(CommandContext<FabricClientCommandSource> commandContext) {
        restoreWaypoints();
        var name = commandContext.getArgument("name",String.class);
        commandWaypoints.remove(name);
        saveWaypoints();
        return 0;
    }


    private int addWaypointCommand(CommandContext<FabricClientCommandSource> commandContext) {
        restoreWaypoints();
        var name = commandContext.getArgument("name",String.class);
        if(commandWaypoints.containsKey(name)){
            throw new RuntimeException(String.format("This waypoint already exists in dimension \"%s\"",commandWaypoints.get(name).getDimension()));
        }
        var coords = getArgumentOrNull(commandContext,"coords", PosArgument.class);
        var dimensionString = getDimensionFromContext(commandContext);
        if(dimensionString==null)
            dimensionString= DynMapHelper.getCurrentWorld().toString();

        Vec3d finalCoords;
        if(coords!=null)
        {
            finalCoords = getPosArgumentCoords(coords,commandContext);
        }
        else {
            finalCoords = commandContext.getSource().getClient().player.getPos();
        }
        addCommandWaypoint(name,finalCoords,dimensionString);
        saveWaypoints();
        return 0;
    }

    private String getDimensionFromContext(CommandContext<FabricClientCommandSource> commandContext) {
        var dimension = getArgumentOrNull(commandContext,"dimension", Identifier.class);
        var dimensionString = getArgumentOrNull(commandContext,"dimension_name", String.class);

        if(dimensionString==null){
            if(dimension!=null)
                dimensionString=DynMapHelper.registryKeyToString(dimension);
        }
        return dimensionString;
    }

    private Vec3d getPosArgumentCoords(PosArgument coords, CommandContext<FabricClientCommandSource> commandContext) {
        var fabricSource = commandContext.getSource();
        var commnandSource = new ServerCommandSource(
                null,
                fabricSource.getPosition(),
                fabricSource.getRotation(),
                null,
                0,
                "",
                Text.literal(""),
                fabricSource.getClient().getServer(),
                fabricSource.getEntity()
        );
        return coords.toAbsolutePos(commnandSource);
    }

    private <T> T getArgumentOrNull(CommandContext<FabricClientCommandSource> commandContext,String name,Class<T> type){
        try{
            return commandContext.getArgument(name,type);
        }
        catch (Exception e){
            return null;
        }
    }
    private void addCommandWaypoint(String name, Vec3d coords, String dimension){
        var waypoint = new CommandWaypoint(coords,name,ColorHelper.Argb.getArgb(255,51, 204, 92),dimension);
        commandWaypoints.put(name,waypoint);
    }

    public List<Waypoint> getAllWaypoints(){
        List<Waypoint> waypoints = new LinkedList<>();
        waypoints.addAll(players);
        waypoints.add(clientWaypoint);
        var values = commandWaypoints.values();
        waypoints.addAll(values);
        return waypoints;
    }

    public Waypoint getClientWaypoint() {
        return clientWaypoint;
    }
}
