package com.tr4nce.rabbittools.creative;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RabbitTools.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONSTRUCTION_WAND_TAB =
            CREATIVE_TABS.register("construction_wand_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + RabbitTools.MODID))
                    .icon(() -> new ItemStack(ModItems.WAND_INFINITY.get()))
                    .displayItems((parameters, output) -> {
                        for (DeferredHolder<Item, ? extends Item> entry : ModItems.ITEMS.getEntries()) {
                            output.accept(entry.get());
                        }
                        com.tr4nce.rabbittools.item.HammerItems.ITEMS.getEntries().forEach(entry -> output.accept(entry.get()));
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}