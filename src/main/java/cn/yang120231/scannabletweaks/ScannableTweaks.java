package cn.yang120231.scannabletweaks;

import cn.yang120231.scannabletweaks.slots.ScannableTweaksDataComponents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Scannable ships no recipe-viewer integration, and the five block slots in its configurable
 * block scanner module are hand-drawn rectangles rather than real container slots, so JEI,
 * REI, and EMI have nothing to drop onto. This mod adds the missing drop targets, and makes
 * the five slots behave as five independent positions while it is at it.
 *
 * @see cn.yang120231.scannabletweaks.jei.ScannableTweaksJeiPlugin
 * @see cn.yang120231.scannabletweaks.rei.ScannableTweaksReiPlugin
 * @see cn.yang120231.scannabletweaks.emi.ScannableTweaksEmiPlugin
 * @see cn.yang120231.scannabletweaks.slots.BlockSlotLayout
 */
@Mod(ScannableTweaks.MOD_ID)
public final class ScannableTweaks {
    public static final String MOD_ID = "scannable_tweaks";

    public ScannableTweaks(final IEventBus modBus) {
        ScannableTweaksDataComponents.register(modBus);
    }
}
