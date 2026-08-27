package cn.yang120231.scannabletweaks.slots;

import com.mojang.serialization.Codec;
import cn.yang120231.scannabletweaks.ScannableTweaks;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ScannableTweaksDataComponents {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ScannableTweaks.MOD_ID);

    /**
     * The slot each entry of Scannable's own {@code scannable:blocks} component occupies,
     * so a configured block can sit in slot 4 while slots 2 and 3 stay empty.
     * <p>
     * Scannable stores configured blocks as a dense list and renders it packed from slot 0,
     * which is why removing an entry pulls every later entry forward. Rather than pad that
     * list with placeholder ids — which would pollute another mod's data and keep occupying
     * slots after this mod is uninstalled — we keep the positions beside it, in a component
     * of our own. Scannable's list stays a plain list of real blocks, so its scan filter,
     * tooltip and anything else reading it are untouched.
     * <p>
     * Written only when the layout has gaps: an item configured left-to-right carries no
     * component of ours at all and is byte-identical to one from an unmodified Scannable.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<Integer>>> BLOCK_SLOTS =
        COMPONENTS.register("block_slots", () -> DataComponentType.<List<Integer>>builder()
            .persistent(Codec.INT.listOf())
            .networkSynchronized(ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()))
            .build());

    public static void register(final IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    private ScannableTweaksDataComponents() {
    }
}
