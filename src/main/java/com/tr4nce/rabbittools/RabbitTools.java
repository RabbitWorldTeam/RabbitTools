package com.tr4nce.rabbittools;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.tr4nce.rabbittools.basics.ConfigClient;
import com.tr4nce.rabbittools.basics.ConfigServer;
import com.tr4nce.rabbittools.basics.ModStats;
import com.tr4nce.rabbittools.client.ClientEvents;
import com.tr4nce.rabbittools.client.RenderBlockPreview;
import com.tr4nce.rabbittools.client.property.SelectWandCore;
import com.tr4nce.rabbittools.command.TravelStaffCommand;
import com.tr4nce.rabbittools.component.ModDataComponents;
import com.tr4nce.rabbittools.containers.ContainerManager;
import com.tr4nce.rabbittools.containers.ContainerRegistrar;
import com.tr4nce.rabbittools.crafting.ModRecipes;
import com.tr4nce.rabbittools.creative.ModCreativeTabs;
import com.tr4nce.rabbittools.events.VoidSackDropHandler;
import com.tr4nce.rabbittools.events.VoidSackPickupHandler;
import com.tr4nce.rabbittools.items.ModItems;
import com.tr4nce.rabbittools.network.ModMessages;
import com.tr4nce.rabbittools.registry.ModMenuTypes;
import com.tr4nce.rabbittools.wand.undo.UndoHistory;

@Mod(RabbitTools.MODID)
public class RabbitTools {
    public static final String MODID = "rabbittools";
    public static final String MODNAME = "RabbitTools";

    public static RabbitTools instance;
    public static final Logger LOGGER = LogManager.getLogger();

    public ContainerManager containerManager;
    public UndoHistory undoHistory;
    public RenderBlockPreview renderBlockPreview;

    public RabbitTools(IEventBus eventBus, ModContainer container, Dist dist) {
        instance = this;
        com.tr4nce.rabbittools.item.HammerItems.register(eventBus);

        containerManager = new ContainerManager();
        undoHistory = new UndoHistory();

        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(ModMessages::registerPayloads);

        ModDataComponents.DATA_COMPONENT_TYPES.register(eventBus);
        ModItems.ITEMS.register(eventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(eventBus);
        ModCreativeTabs.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModStats.CUSTOM_STATS.register(eventBus);

        container.registerConfig(ModConfig.Type.SERVER, ConfigServer.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ConfigClient.SPEC);

        NeoForge.EVENT_BUS.addListener(TravelStaffCommand::register);
        NeoForge.EVENT_BUS.register(new VoidSackDropHandler());
        NeoForge.EVENT_BUS.register(new VoidSackPickupHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("RabbitTools says hello - may the odds be ever in your favor.");
        ContainerRegistrar.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        renderBlockPreview = new RenderBlockPreview();
        NeoForge.EVENT_BUS.register(renderBlockPreview);
        NeoForge.EVENT_BUS.register(new ClientEvents());

        // Register wand core item property predicate
        SelectWandCore.onClientSetup(event);
    }

    public static ResourceLocation loc(String name) {
        return ResourceLocation.fromNamespaceAndPath(MODID, name);
    }
}