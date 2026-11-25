package cn.yang120231.scannabletweaks.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;

@EmiEntrypoint
public final class ScannableTweaksEmiPlugin implements EmiPlugin {
    @Override
    public void register(final EmiRegistry registry) {
        registry.addDragDropHandler(
            ConfigurableBlockScannerModuleContainerScreen.class,
            new BlockModuleEmiDropHandler());
    }
}
