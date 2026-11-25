package cn.yang120231.scannabletweaks.rei;

import cn.yang120231.scannabletweaks.slots.BlockModuleDropHelper;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Exposes the block scanner module's five configuration slots as REI drop targets.
 */
public final class BlockModuleReiDropVisitor
    implements DraggableStackVisitor<ConfigurableBlockScannerModuleContainerScreen> {

    @Override
    public <R extends Screen> boolean isHandingScreen(final R screen) {
        return screen instanceof ConfigurableBlockScannerModuleContainerScreen;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(
        final DraggingContext<ConfigurableBlockScannerModuleContainerScreen> context,
        final DraggableStack stack
    ) {
        if (resolveBlockId(stack).isEmpty()) {
            return Stream.empty();
        }

        return BlockModuleDropHelper.slotBounds(context.getScreen()).stream()
            .map(slot -> BoundsProvider.ofRectangle(
                new Rectangle(slot.x(), slot.y(), slot.width(), slot.height())));
    }

    @Override
    public DraggedAcceptorResult acceptDraggedStack(
        final DraggingContext<ConfigurableBlockScannerModuleContainerScreen> context,
        final DraggableStack stack
    ) {
        final ResourceLocation blockId = resolveBlockId(stack).orElse(null);
        if (blockId == null) {
            return DraggedAcceptorResult.PASS;
        }

        final Point pos = context.getCurrentPosition();
        if (pos == null) {
            return DraggedAcceptorResult.PASS;
        }

        final ConfigurableBlockScannerModuleContainerScreen screen = context.getScreen();
        for (final BlockModuleDropHelper.SlotBounds slot : BlockModuleDropHelper.slotBounds(screen)) {
            if (slot.contains(pos.x, pos.y)) {
                BlockModuleDropHelper.sendSetBlock(screen, slot.index(), blockId);
                return DraggedAcceptorResult.ACCEPTED;
            }
        }
        return DraggedAcceptorResult.PASS;
    }

    private static Optional<ResourceLocation> resolveBlockId(final DraggableStack stack) {
        final EntryStack<?> entry = stack.getStack();
        if (entry.getType() != VanillaEntryTypes.ITEM) {
            return Optional.empty();
        }
        final ItemStack itemStack = entry.castValue();
        return BlockModuleDropHelper.resolveBlockId(itemStack);
    }
}
