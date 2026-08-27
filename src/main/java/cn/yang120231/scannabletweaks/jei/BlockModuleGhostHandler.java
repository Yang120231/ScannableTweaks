package cn.yang120231.scannabletweaks.jei;

import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtMessage;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_X;
import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_Y;
import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOT_SIZE;

/**
 * Exposes the block scanner module's five configuration slots as JEI drop targets.
 *
 * The slots are not real container slots, so a dropped ingredient cannot travel through
 * the usual inventory click path. Instead we mirror what Scannable's own
 * {@code mouseClicked} handler does: resolve the stack to a block and send Scannable's
 * existing {@link SetConfiguredModuleItemAtMessage} to the server.
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
        final ResourceLocation blockId = resolveBlockId(ingredient).orElse(null);
        if (blockId == null) {
            return List.of();
        }

        // SLOTS_ORIGIN_X/_Y point at the inner item rectangle (a 16×16 item rendered
        // with a 1 px black border on the left/top edges), so the slot's visible
        // frame is actually 1 px to the upper-left of that anchor. Returning the
        // inner coordinates as-is makes JEI's ghost highlight sit 1 px down and to
        // the right of the slot border, so shift it back by 1 px to align.
        final int originX = gui.getGuiLeft() + SLOTS_ORIGIN_X - 1;
        final int originY = gui.getGuiTop() + SLOTS_ORIGIN_Y - 1;
        final int containerId = gui.getMenu().containerId;

        final List<Target<I>> targets = new ArrayList<>(Constants.CONFIGURABLE_MODULE_SLOTS);
        for (int slot = 0; slot < Constants.CONFIGURABLE_MODULE_SLOTS; slot++) {
            final Rect2i area = new Rect2i(originX + slot * SLOT_SIZE, originY, SLOT_SIZE, SLOT_SIZE);
            targets.add(new SlotTarget<>(area, containerId, slot, blockId));
        }
        return targets;
    }

    @Override
    public void onComplete() {
    }

    private static Optional<ResourceLocation> resolveBlockId(final ITypedIngredient<?> ingredient) {
        return ingredient.getItemStack()
            .map(stack -> Block.byItem(stack.getItem()))
            .filter(block -> block != Blocks.AIR)
            .flatMap(block -> BuiltInRegistries.BLOCK.getResourceKey(block))
            .map(ResourceKey::location);
    }

    private record SlotTarget<I>(
        Rect2i area,
        int containerId,
        int slot,
        ResourceLocation blockId
    ) implements Target<I> {
        @Override
        public Rect2i getArea() {
            return area;
        }

        @Override
        public void accept(final I ingredient) {
            Network.sendToServer(new SetConfiguredModuleItemAtMessage(containerId, slot, blockId));
        }
    }
}
