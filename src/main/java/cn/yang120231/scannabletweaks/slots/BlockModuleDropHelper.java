package cn.yang120231.scannabletweaks.slots;

import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.network.Network;
import li.cil.scannable.common.network.message.SetConfiguredModuleItemAtMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_X;
import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOTS_ORIGIN_Y;
import static li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen.SLOT_SIZE;

/**
 * Shared client-side drop geometry, block resolution, and packet dispatch for the block
 * scanner module's five configuration slots across JEI, REI, and EMI.
 */
public final class BlockModuleDropHelper {
    public static List<SlotBounds> slotBounds(final ConfigurableBlockScannerModuleContainerScreen screen) {
        // SLOTS_ORIGIN_X/_Y point at the inner 16x16 item rectangle (1 px inside the slot
        // frame), so shift back by 1 px to align drop targets and highlights with the border.
        final int originX = screen.getGuiLeft() + SLOTS_ORIGIN_X - 1;
        final int originY = screen.getGuiTop() + SLOTS_ORIGIN_Y - 1;

        final List<SlotBounds> targets = new ArrayList<>(Constants.CONFIGURABLE_MODULE_SLOTS);
        for (int slot = 0; slot < Constants.CONFIGURABLE_MODULE_SLOTS; slot++) {
            targets.add(new SlotBounds(slot, originX + slot * SLOT_SIZE, originY, SLOT_SIZE, SLOT_SIZE));
        }
        return targets;
    }

    public static Optional<ResourceLocation> resolveBlockId(final ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BlockSlotLayout.idOf(Block.byItem(stack.getItem())));
    }

    public static void sendSetBlock(
        final ConfigurableBlockScannerModuleContainerScreen screen,
        final int slot,
        final ResourceLocation blockId
    ) {
        Network.sendToServer(new SetConfiguredModuleItemAtMessage(screen.getMenu().containerId, slot, blockId));
    }

    public record SlotBounds(int index, int x, int y, int width, int height) {
        public boolean contains(final double mouseX, final double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    private BlockModuleDropHelper() {
    }
}
