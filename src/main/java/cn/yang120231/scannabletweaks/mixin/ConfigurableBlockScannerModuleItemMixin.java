package cn.yang120231.scannabletweaks.mixin;

import cn.yang120231.scannabletweaks.slots.BlockSlotLayout;
import li.cil.scannable.common.item.ConfigurableBlockScannerModuleItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the five configuration slots independent positions rather than a dense list.
 * <p>
 * Scannable keeps configured blocks in a plain {@code List<ResourceLocation>} and the GUI
 * renders it packed from slot 0, so removing an entry slides every later entry forward and
 * there is no way to express "slot 3 is empty, slot 4 is not". These three methods are the
 * only places that list is edited, so replacing them is enough to make every path —
 * clicking a slot, dropping onto one from JEI, right-clicking a block in the world —
 * position-preserving.
 * <p>
 * All three run on whichever side owns the item, so this must be installed on a dedicated
 * server for gaps to survive there. Without it the server keeps Scannable's dense
 * behaviour, no layout component is ever written, and clients fall back to rendering the
 * list packed exactly as they do today.
 *
 * @see BlockSlotLayout
 */
@Mixin(ConfigurableBlockScannerModuleItem.class)
public abstract class ConfigurableBlockScannerModuleItemMixin {
    @Inject(
        method = "setBlockAt(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/block/Block;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void scannableTweaks$setBlockAtPositionally(
        final ItemStack stack,
        final int index,
        final Block block,
        final CallbackInfo ci
    ) {
        ci.cancel();

        if (index < 0 || index >= BlockSlotLayout.SLOT_COUNT) {
            return;
        }
        if (ConfigurableBlockScannerModuleItem.isLocked(stack)) {
            return;
        }

        final ResourceLocation id = BlockSlotLayout.idOf(block);
        if (id == null) {
            return;
        }

        final ResourceLocation[] slots = BlockSlotLayout.read(stack);
        if (id.equals(slots[index])) {
            return;
        }

        // Configuring a block that is already in another slot moves it, which is what
        // Scannable intends too. It expressed that as "assign, then remove the old list
        // index", which could delete an unrelated entry: with [stone, dirt, sand],
        // dropping stone on slot 3 overwrote sand and then removed stone's old entry,
        // leaving [dirt, stone] and destroying sand. Clearing by position cannot.
        for (int slot = 0; slot < slots.length; slot++) {
            if (id.equals(slots[slot])) {
                slots[slot] = null;
            }
        }
        slots[index] = id;

        BlockSlotLayout.write(stack, slots);
    }

    @Inject(
        method = "removeBlockAt(Lnet/minecraft/world/item/ItemStack;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void scannableTweaks$removeBlockAtPositionally(
        final ItemStack stack,
        final int index,
        final CallbackInfo ci
    ) {
        ci.cancel();

        if (index < 0 || index >= BlockSlotLayout.SLOT_COUNT) {
            return;
        }
        if (ConfigurableBlockScannerModuleItem.isLocked(stack)) {
            return;
        }

        final ResourceLocation[] slots = BlockSlotLayout.read(stack);
        if (slots[index] == null) {
            return;
        }

        slots[index] = null;
        BlockSlotLayout.write(stack, slots);
    }

    /**
     * Right-clicking a block in the world adds it to the module. Upstream appended to the
     * list and reported "no free slots" once it held five entries; with gaps allowed, the
     * free slot to fill is the first empty one, and fullness has to be counted by slot.
     */
    @Inject(
        method = "addBlock(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/Block;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void scannableTweaks$addBlockPositionally(
        final ItemStack stack,
        final Block block,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        final ResourceLocation id = BlockSlotLayout.idOf(block);
        if (id == null) {
            cir.setReturnValue(false);
            return;
        }
        if (ConfigurableBlockScannerModuleItem.isLocked(stack)) {
            cir.setReturnValue(false);
            return;
        }

        final ResourceLocation[] slots = BlockSlotLayout.read(stack);
        for (final ResourceLocation configured : slots) {
            if (id.equals(configured)) {
                cir.setReturnValue(true);
                return;
            }
        }

        for (int slot = 0; slot < slots.length; slot++) {
            if (slots[slot] == null) {
                slots[slot] = id;
                BlockSlotLayout.write(stack, slots);
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }
}
