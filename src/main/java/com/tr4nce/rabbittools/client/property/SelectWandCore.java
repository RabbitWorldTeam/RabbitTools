package com.tr4nce.rabbittools.client.property;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.basics.option.WandOptions;
import com.tr4nce.rabbittools.items.ModItems;
import com.tr4nce.rabbittools.items.core.ItemCoreAngel;
import com.tr4nce.rabbittools.items.core.ItemCoreDestruction;
import com.tr4nce.rabbittools.items.core.ItemCoreExchange;
import com.tr4nce.rabbittools.items.wand.ItemWand;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredItem;

public class SelectWandCore
{
    public static final ResourceLocation WAND_CORE = ResourceLocation.fromNamespaceAndPath(RabbitTools.MODID, "wand_core");

    public static void onClientSetup(FMLClientSetupEvent event) {
        // ItemProperties#register is not thread-safe, must run inside enqueueWork
        event.enqueueWork(() -> {
            for (DeferredItem<Item> wand : ModItems.WANDS) {
                ItemProperties.register(wand.get(), WAND_CORE, (stack, level, entity, seed) -> {
                    if (!(stack.getItem() instanceof ItemWand)) return 0f;
                    var core = new WandOptions(stack).cores.get();
                    if (core instanceof ItemCoreAngel)       return 1f; // core_angel active
                    if (core instanceof ItemCoreDestruction) return 2f; // core_destruction active
                    if (core instanceof ItemCoreExchange)    return 3f; // core_exchange active
                    return 0f;                                          // default (no core)
                });
            }
        });
    }
}