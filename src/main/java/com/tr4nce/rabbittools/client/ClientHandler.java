package com.tr4nce.rabbittools.client;

import com.tr4nce.rabbittools.RabbitTools;
import com.tr4nce.rabbittools.client.screen.PaintbrushScreen;
import com.tr4nce.rabbittools.client.screen.ScreenVoidSack;
import com.tr4nce.rabbittools.item.HammerItems;
import com.tr4nce.rabbittools.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = RabbitTools.MODID, value = Dist.CLIENT)
public class ClientHandler {


    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(new KeybindHandler());
    }

    @SubscribeEvent
    public static void registerHammerColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> 0xFF777081, HammerItems.NETHERITE_MINING_HAMMER.get());
        event.register((stack, layer) -> 0xFFC4D9F0, HammerItems.NEUTRONIUM_MINING_HAMMER.get());
    }

    @SubscribeEvent
    public static void registerKeymapping(final RegisterKeyMappingsEvent event) {
        event.register(KeybindHandler.KEY_VOID_SACK_TOGGLE);
        event.register(KeybindHandler.KEY_WAND_UNDO);
        event.register(KeybindHandler.KEY_EXCHANGE_SELECT);
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.PAINTBRUSH.get(), PaintbrushScreen::new);
        event.register(ModMenuTypes.VOID_SACK.get(), ScreenVoidSack::new);
    }
}