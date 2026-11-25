package cn.yang120231.scannabletweaks.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public final class ScannableTweaksReiPlugin implements REIClientPlugin {
    @Override
    public void registerScreens(final ScreenRegistry registry) {
        registry.registerDraggableStackVisitor(new BlockModuleReiDropVisitor());
    }
}
