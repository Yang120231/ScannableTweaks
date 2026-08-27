package cn.yang120231.scannabletweaks.mixin.client;

import cn.yang120231.scannabletweaks.slots.BlockSlotLayout;
import li.cil.scannable.client.gui.ConfigurableBlockScannerModuleContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the block module's slots at the positions blocks actually occupy.
 * <p>
 * The screen renders its list of configured items packed from slot 0, so with the data now
 * allowing gaps it has to be handed one entry per slot rather than one entry per configured
 * block. {@link Blocks#AIR} stands in for an empty slot: the list stays the
 * {@code List<Block>} the screen expects, and both of its consumers — the item drawn in
 * {@code renderLabels} and the name shown as a tooltip in {@code render} — are short-
 * circuited for it below.
 */
@Mixin(ConfigurableBlockScannerModuleContainerScreen.class)
public abstract class ConfigurableBlockScannerModuleContainerScreenMixin {
    @Inject(
        method = "getConfiguredItems(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void scannableTweaks$listItemsBySlot(final ItemStack stack, final CallbackInfoReturnable<List<Block>> cir) {
        final ResourceLocation[] slots = BlockSlotLayout.read(stack);

        final List<Block> items = new ArrayList<>(slots.length);
        for (final ResourceLocation id : slots) {
            items.add(id == null
                ? Blocks.AIR
                : BuiltInRegistries.BLOCK.getOptional(id).orElse(Blocks.AIR));
        }

        cir.setReturnValue(items);
    }

    @Inject(
        method = "getItemName(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/network/chat/Component;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void scannableTweaks$noNameForEmptySlot(final Block block, final CallbackInfoReturnable<Component> cir) {
        if (block == Blocks.AIR) {
            cir.setReturnValue(BlockSlotLayout.EMPTY_SLOT_NAME);
        }
    }

    @Inject(
        method = "renderConfiguredItem(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/level/block/Block;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void scannableTweaks$drawNothingForEmptySlot(
        final GuiGraphics graphics,
        final Block block,
        final int x,
        final int y,
        final CallbackInfo ci
    ) {
        if (block == Blocks.AIR) {
            ci.cancel();
        }
    }
}
