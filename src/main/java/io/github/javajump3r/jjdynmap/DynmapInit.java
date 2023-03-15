package io.github.javajump3r.jjdynmap;

import io.github.javajump3r.jjdynmap.dynmap.DynMapRenderer;
import io.github.javajump3r.jjdynmap.dynmap.DynmapPlayers;
import io.github.javajump3r.jjdynmap.dynmap.dynmapscreen.DynmapWorldScreen;
import io.github.javajump3r.jjdynmap.dynmap.waypoints.WaypointStorage;
import io.github.javajumper.lavajumper.LavaJumper;
import io.github.javajumper.lavajumper.common.Binder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public class DynmapInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new DynmapPlayers();
		new DynMapRenderer();
		Binder.addBind("Open Worldmap", "Dynmap client", -1, client -> {
			client.setScreen(new DynmapWorldScreen(Text.empty()));
		});
		WaypointStorage.getMainInstance();
	}
}
