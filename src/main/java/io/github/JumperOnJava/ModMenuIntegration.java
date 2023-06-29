package io.github.JumperOnJava;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.JumperOnJava.lavajumper.LavaJumper;
import net.minecraft.client.gui.screen.Screen;

public class ModMenuIntegration implements ModMenuApi {
    public ConfigScreenFactory<Screen> getModConfigScreenFactory(){
        return LavaJumper.getConfig()::getFinishedConfigScreen;
    }

}
