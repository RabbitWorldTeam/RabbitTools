package com.tr4nce.rabbittools.items.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class MeasuringTapeItem extends Item {
    private static final String DIMENSION = "measurement_dimension";
    private static final String START = "measurement_start";
    private static final String END = "measurement_end";

    public MeasuringTapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || player.isSpectator()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                clear(stack);
            } else {
                var pos = context.getClickedPos();
                var start = start(stack, level);
                if (start == null || end(stack, level) != null) {
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
                        data.putString(DIMENSION, level.dimension().location().toString());
                        data.putLong(START, pos.asLong());
                        data.remove(END);
                    });
                } else {
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putLong(END, pos.asLong()));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || player.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide) {
            clear(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void clear(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            data.remove(DIMENSION);
            data.remove(START);
            data.remove(END);
        });
    }

    @Nullable
    public static BlockPos start(ItemStack stack, Level level) {
        return read(stack, level, START);
    }

    @Nullable
    public static BlockPos end(ItemStack stack, Level level) {
        return read(stack, level, END);
    }

    @Nullable
    private static BlockPos read(ItemStack stack, Level level, String key) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.getString(DIMENSION).equals(level.dimension().location().toString()) && data.contains(key, Tag.TAG_LONG)
                ? BlockPos.of(data.getLong(key)) : null;
    }

    public static Component dimensions(BlockPos a, BlockPos b) {
        int x = Math.abs(a.getX() - b.getX());
        int y = Math.abs(a.getY() - b.getY());
        int z = Math.abs(a.getZ() - b.getZ());
        return Component.translatable("hud.rabbittools.measurement.dimensions", x + 1, y + 1, z + 1,
                Math.max(0, x - 1), Math.max(0, y - 1), Math.max(0, z - 1));
    }

    public static Vec3 center(BlockPos a, BlockPos b) {
        return Vec3.atCenterOf(a).lerp(Vec3.atCenterOf(b), 0.5);
    }

    public static BlockPos centerBlock(BlockPos a, BlockPos b, boolean upper) {
        int offset = upper ? 1 : 0;
        return new BlockPos(Math.floorDiv(a.getX() + b.getX() + offset, 2),
                Math.floorDiv(a.getY() + b.getY() + offset, 2),
                Math.floorDiv(a.getZ() + b.getZ() + offset, 2));
    }
}
