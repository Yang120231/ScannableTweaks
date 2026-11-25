package cn.yang120231.scannabletweaks.emi;

import cn.yang120231.scannabletweaks.slots.BlockModuleDropHelper;
import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * Exposes the block scanner module's five configuration slots as EMI drop targets.
 */
public final class BlockModuleEmiDropHandler
    implements EmiDragDropHandler<ConfigurableBlockScannerModuleContainerScreen> {

    private static final int HIGHLIGHT_COLOR = 0x8822BB33;

    @Override
    public boolean dropStack(
        final ConfigurableBlockScannerModuleContainerScreen screen,
        final EmiIngredient stack,
        final int x,
        final int y
    ) {
        final ResourceLocation blockId = resolveBlockId(stack).orElse(null);
        if (blockId == null) {
            return false;
        }

        for (final BlockModuleDropHelper.SlotBounds slot : BlockModuleDropHelper.slotBounds(screen)) {
            if (slot.contains(x, y)) {
                BlockModuleDropHelper.sendSetBlock(screen, slot.index(), blockId);
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(
        final ConfigurableBlockScannerModuleContainerScreen screen,
        final EmiIngredient dragged,
        final GuiGraphics graphics,
        final int mouseX,
        final int mouseY,
        final float delta
    ) {
        if (resolveBlockId(dragged).isEmpty()) {
            return;
        }

        for (final BlockModuleDropHelper.SlotBounds slot : BlockModuleDropHelper.slotBounds(screen)) {
            graphics.fill(
                slot.x(),
                slot.y(),
                slot.x() + slot.width(),
                slot.y() + slot.height(),
                HIGHLIGHT_COLOR);
        }
    }

    private static Optional<ResourceLocation> resolveBlockId(final EmiIngredient ingredient) {
        final List<EmiStack> stacks = ingredient.getEmiStacks();
        if (stacks.size() != 1) {
            return Optional.empty();
        }
        return BlockModuleDropHelper.resolveBlockId(stacks.getFirst().getItemStack());
    }
}
