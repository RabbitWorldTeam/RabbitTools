package com.tr4nce.rabbittools.item;

import com.tr4nce.rabbittools.RabbitTools;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HammerItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RabbitTools.MODID);
    private static final Tier COPPER = new SimpleTier(
            Tiers.STONE.getIncorrectBlocksForDrops(), 190, 5.0F, 1.5F, 12,
            () -> Ingredient.of(Items.COPPER_INGOT)
    );

    public static final DeferredItem<MiningHammerItem> NETHERITE_MINING_HAMMER;
    public static final DeferredItem<MiningHammerItem> NEUTRONIUM_MINING_HAMMER;
    public static final DeferredItem<Item> NEUTRONIUM_INGOT;

    static {
        registerHammer("iron", Tiers.IRON);
        registerHammer("copper", COPPER);
        registerHammer("gold", Tiers.GOLD);
        registerHammer("diamond", Tiers.DIAMOND);
        NETHERITE_MINING_HAMMER = ITEMS.register("netherite_mining_hammer",
                () -> new MiningHammerItem(Tiers.NETHERITE, hammerProperties(Tiers.NETHERITE).fireResistant()));
        NEUTRONIUM_MINING_HAMMER = ITEMS.register("neutronium_mining_hammer",
                () -> new MiningHammerItem(Tiers.NETHERITE, hammerProperties(Tiers.NETHERITE).fireResistant()
                        .component(DataComponents.UNBREAKABLE, new Unbreakable(true))));
        NEUTRONIUM_INGOT = ITEMS.register("neutronium_ingot", () -> new Item(new Item.Properties().fireResistant()));
    }

    private HammerItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(HammerItems::addCreativeTabItems);
    }

    private static void registerHammer(String material, Tier tier) {
        ITEMS.register(material + "_mining_hammer", () -> new MiningHammerItem(tier, hammerProperties(tier)));
    }

    private static Item.Properties hammerProperties(Tier tier) {
        return new Item.Properties().attributes(MiningHammerItem.createAttributes(tier, 3.0F, -3.2F));
    }

    private static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)) {
            ITEMS.getEntries().stream().map(item -> item.get())
                    .filter(MiningHammerItem.class::isInstance).forEach(event::accept);
        }
    }
}
