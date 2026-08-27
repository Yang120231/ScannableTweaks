package cn.yang120231.scannabletweaks.jei;

import cn.yang120231.scannabletweaks.ScannableTweaks;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class ScannableTweaksJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
        ResourceLocation.fromNamespaceAndPath(ScannableTweaks.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(
            ConfigurableBlockScannerModuleContainerScreen.class,
            new BlockModuleGhostHandler());
    }
}
