package cn.yang120231.scannabletweaks.jei;

import cn.yang120231.scannabletweaks.slots.BlockModuleDropHelper;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Exposes the block scanner module's five configuration slots as JEI drop targets.
 */
public final class BlockModuleGhostHandler
    implements IGhostIngredientHandler<ConfigurableBlockScannerModuleContainerScreen> {

    @Override
    public <I> List<Target<I>> getTargetsTyped(
        final ConfigurableBlockScannerModuleContainerScreen gui,
        final ITypedIngredient<I> ingredient,
        final boolean doStart
    ) {
        // Offering targets for a non-block would let players drop an ingredient that
        // Scannable then silently discards, so filter it out the same way it does.
        final ResourceLocation blockId = ingredient.getItemStack()
            .flatMap(BlockModuleDropHelper::resolveBlockId)
            .orElse(null);
        if (blockId == null) {
            return List.of();
        }

        return BlockModuleDropHelper.slotBounds(gui).stream()
            .<Target<I>>map(slot -> new SlotTarget<>(
                gui,
                new Rect2i(slot.x(), slot.y(), slot.width(), slot.height()),
                slot.index(),
                blockId))
            .toList();
    }

    @Override
    public void onComplete() {
    }

    private record SlotTarget<I>(
        ConfigurableBlockScannerModuleContainerScreen screen,
        Rect2i area,
        int slot,
        ResourceLocation blockId
    ) implements Target<I> {
        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(final I ingredient) {
            BlockModuleDropHelper.sendSetBlock(screen, slot, blockId);
        }
    }
}
