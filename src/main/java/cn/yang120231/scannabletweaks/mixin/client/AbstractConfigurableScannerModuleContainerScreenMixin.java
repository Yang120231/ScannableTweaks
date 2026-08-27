package cn.yang120231.scannabletweaks.mixin.client;

import cn.yang120231.scannabletweaks.slots.BlockSlotLayout;
import li.cil.scannable.client.gui.AbstractConfigurableScannerModuleContainerScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Suppresses the hover tooltip over an empty slot.
 * <p>
 * The screen shows a tooltip for every slot it has an entry for, and empty slots now have
 * one — {@link BlockSlotLayout#EMPTY_SLOT_NAME}, which would otherwise draw as a small
 * blank frame. The marker is matched by identity, so the entity module, which shares this
 * screen but has no gaps, is unaffected.
 */
@Mixin(AbstractConfigurableScannerModuleContainerScreen.class)
public abstract class AbstractConfigurableScannerModuleContainerScreenMixin {
    @Redirect(
        method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;II)V"
        )
    )
    private void scannableTweaks$skipEmptySlotTooltip(
        final GuiGraphics graphics,
        final Font font,
        final Component text,
        final int mouseX,
        final int mouseY
    ) {
        if (text == BlockSlotLayout.EMPTY_SLOT_NAME) {
            return;
        }
        graphics.renderTooltip(font, text, mouseX, mouseY);
    }
}
