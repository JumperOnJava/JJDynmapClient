package io.github.JumperOnJava.jjdynmap;

import io.github.JumperOnJava.jjdynmap.dynmap.DynMapRenderer;
import io.github.JumperOnJava.jjdynmap.dynmap.DynmapPlayers;
import io.github.JumperOnJava.jjdynmap.dynmap.dynmapscreen.DynmapWorldScreen;
import io.github.JumperOnJava.jjdynmap.dynmap.waypoints.WaypointStorage;
import io.github.JumperOnJava.lavajumper.LavaJumper;
import io.github.JumperOnJava.lavajumper.common.Binder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.Text;

public class DynmapInit implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new DynmapPlayers();
		new DynMapRenderer();
		LavaJumper.getConfig().restoreConfig();
		Binder.addBind("Open Worldmap", "Dynmap client", -1, client -> {
			client.setScreen(new DynmapWorldScreen(Text.empty()));
		});
		WaypointStorage.getMainInstance();
	}
}
