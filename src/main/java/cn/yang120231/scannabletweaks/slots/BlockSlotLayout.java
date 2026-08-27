package cn.yang120231.scannabletweaks.slots;

import li.cil.scannable.common.config.Constants;
import li.cil.scannable.common.item.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes a block scanner module's five configuration slots as five independent
 * positions instead of the dense list Scannable stores them in.
 * <p>
 * A layout is a {@value #SLOT_COUNT}-element array of block ids where {@code null} means
 * "empty slot". It is assembled from two components: Scannable's {@code scannable:blocks}
 * (the blocks themselves, still a plain dense list) and our own
 * {@link ScannableTweaksDataComponents#BLOCK_SLOTS} (the slot each of those blocks sits in).
 */
public final class BlockSlotLayout {
    public static final int SLOT_COUNT = Constants.CONFIGURABLE_MODULE_SLOTS;

    /**
     * Returned by the GUI as the name of an empty slot, and recognised by identity so the
     * hover tooltip can be suppressed instead of drawing an empty frame.
     */
    public static final Component EMPTY_SLOT_NAME = Component.empty();

    // --------------------------------------------------------------------- //

    /**
     * @return the configured blocks by slot; {@code null} entries are empty slots.
     */
    public static ResourceLocation[] read(final ItemStack stack) {
        final ResourceLocation[] slots = new ResourceLocation[SLOT_COUNT];

        final List<ResourceLocation> ids = stack.getOrDefault(ModDataComponents.BLOCKS.get(), List.of());
        if (ids.isEmpty()) {
            return slots;
        }

        final List<Integer> positions = stack.get(ScannableTweaksDataComponents.BLOCK_SLOTS.get());
        if (!isUsable(positions, ids.size())) {
            // Either the item has never been given a layout (no gaps yet, or it comes from
            // a world without this mod), or something we do not intercept has edited
            // Scannable's list out from under us. Fall back to its dense packing, which is
            // exactly how an unmodified Scannable would render the item.
            for (int slot = 0; slot < Math.min(ids.size(), SLOT_COUNT); slot++) {
                slots[slot] = ids.get(slot);
            }
            return slots;
        }

        for (int i = 0; i < ids.size(); i++) {
            final ResourceLocation id = ids.get(i);
            // Air would render as an empty slot but still occupy one, leaving a slot that
            // looks free yet cannot be filled. Drop it; the next write cleans it up.
            if (!isAir(id)) {
                slots[positions.get(i)] = id;
            }
        }
        return slots;
    }

    /**
     * Stores the given layout, keeping Scannable's own component authoritative for
     * <em>which</em> blocks are configured and ours for <em>where</em> they sit.
     */
    public static void write(final ItemStack stack, final ResourceLocation[] slots) {
        final List<ResourceLocation> ids = new ArrayList<>(SLOT_COUNT);
        final List<Integer> positions = new ArrayList<>(SLOT_COUNT);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            if (slots[slot] != null) {
                ids.add(slots[slot]);
                positions.add(slot);
            }
        }

        // Scannable writes an empty list rather than dropping the component; match it.
        stack.set(ModDataComponents.BLOCKS.get(), List.copyOf(ids));

        if (isPacked(positions)) {
            stack.remove(ScannableTweaksDataComponents.BLOCK_SLOTS.get());
        } else {
            stack.set(ScannableTweaksDataComponents.BLOCK_SLOTS.get(), List.copyOf(positions));
        }
    }

    /**
     * @return the id of the given block, or {@code null} if it cannot be configured.
     */
    @Nullable
    public static ResourceLocation idOf(final Block block) {
        if (block == Blocks.AIR) {
            return null;
        }
        return BuiltInRegistries.BLOCK.getResourceKey(block).map(ResourceKey::location).orElse(null);
    }

    // --------------------------------------------------------------------- //

    /**
     * A layout only describes the list it was written for. The entry count is enough to
     * catch the case that matters — a server without this mod having removed an entry from
     * Scannable's list, leaving our positions one too long.
     */
    private static boolean isUsable(@Nullable final List<Integer> positions, final int idCount) {
        if (positions == null || positions.size() != idCount || idCount > SLOT_COUNT) {
            return false;
        }

        final boolean[] taken = new boolean[SLOT_COUNT];
        for (final int position : positions) {
            if (position < 0 || position >= SLOT_COUNT || taken[position]) {
                return false;
            }
            taken[position] = true;
        }
        return true;
    }

    /**
     * @return whether the layout has no gaps, and so needs no component of ours to describe it.
     */
    private static boolean isPacked(final List<Integer> positions) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.get(i) != i) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAir(final ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(null) == Blocks.AIR;
    }

    private BlockSlotLayout() {
    }
}
