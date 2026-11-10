package com.tr4nce.rabbittools.items.tool;

import com.tr4nce.rabbittools.RabbitTools;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = RabbitTools.MODID)
public final class MasonryTrowelItem extends Item {
    private static final Set<Block> STONE = Set.of(
            Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE, Blocks.STONE_BRICKS,
            Blocks.MOSSY_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS,
            Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.POLISHED_ANDESITE,
            Blocks.POLISHED_DIORITE, Blocks.POLISHED_GRANITE, Blocks.COBBLED_DEEPSLATE,
            Blocks.DEEPSLATE, Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.POLISHED_DEEPSLATE,
            Blocks.CRACKED_DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_TILES, Blocks.CHISELED_DEEPSLATE,
            Blocks.BRICKS, Blocks.MUD_BRICKS, Blocks.PACKED_MUD, Blocks.TUFF, Blocks.POLISHED_TUFF,
            Blocks.TUFF_BRICKS, Blocks.CHISELED_TUFF, Blocks.CHISELED_TUFF_BRICKS,
            Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS,
            Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CHISELED_SANDSTONE,
            Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE);

    public MasonryTrowelItem(Properties properties) {
        super(properties);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void placeFromHotbar(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK
                || !(event.getItemStack().getItem() instanceof MasonryTrowelItem)) {
            return;
        }
        var player = event.getPlayer();
        if (player == null || player.isSpectator() || !player.getAbilities().mayBuild) {
            return;
        }

        List<ItemStack> candidates = new ArrayList<>(Inventory.getSelectionSize());
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem item) {
                var state = item.getBlock().defaultBlockState();
                var id = BuiltInRegistries.BLOCK.getKey(item.getBlock());
                if (id.getNamespace().equals("minecraft")
                        && (STONE.contains(item.getBlock()) || state.is(BlockTags.PLANKS)
                        || state.is(BlockTags.TERRACOTTA) || id.getPath().endsWith("_concrete"))) {
                    candidates.add(stack);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        var level = event.getLevel();
        if (level.isClientSide) {
            event.cancelWithResult(ItemInteractionResult.SUCCESS);
            return;
        }

        ItemStack material = candidates.get(player.getRandom().nextInt(candidates.size()));
        UseOnContext original = event.getUseOnContext();
        var hit = new BlockHitResult(original.getClickLocation(), original.getClickedFace(),
                original.getClickedPos(), original.isInside());
        var use = new UseOnContext(level, player, event.getHand(), material, hit);
        var placement = new BlockPlaceContext(use);
        if (!level.mayInteract(player, placement.getClickedPos())
                || !level.getWorldBorder().isWithinBounds(placement.getClickedPos())) {
            event.cancelWithResult(ItemInteractionResult.FAIL);
            return;
        }
        // Run the material's normal placement before NeoForge starts capturing snapshots for the trowel.
        var previous = level.getBlockState(placement.getClickedPos());
        if (material.useOn(use).consumesAction() && level.getBlockState(placement.getClickedPos()) != previous) {
            event.getItemStack().hurtAndBreak(1, player, event.getHand() == InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            event.cancelWithResult(ItemInteractionResult.CONSUME);
        } else {
            event.cancelWithResult(ItemInteractionResult.FAIL);
        }
    }
}
